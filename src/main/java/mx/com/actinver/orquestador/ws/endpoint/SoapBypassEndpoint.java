package mx.com.actinver.orquestador.ws.endpoint;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.util.DynamicProperty;
import mx.com.actinver.orquestador.ws.Decision;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import mx.com.actinver.orquestador.ws.generated.ContestaExpedientexLlaveRequest;
import mx.com.actinver.orquestador.ws.proxy.PassthroughSoapClient;
import mx.com.actinver.orquestador.ws.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.util.BypassRouter;
import mx.com.actinver.orquestador.ws.util.SoapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Controller para enrutar requests SOAP (bypass vs modern).
 * Implementación robusta de unmarshalling usando DOM + JAXB.
 */
@RestController
@RequestMapping("/WSImagenes")
public class SoapBypassEndpoint {

    private static final Logger LOG = LogManager.getLogger(SoapBypassEndpoint.class);

    private PassthroughSoapClient client;

    @Autowired
    private BypassRouter router;

    @Autowired
    private WsImagenesService imagenesService;

    @DynamicProperty("${id-portal.url}")
    private DynamicString idPortalUrl;

    @DynamicProperty("${migration.start.date}")
    private DynamicString migrationCutoverDate;

    // JAXBContext cache (inmutable after init in this simple implementation)
    private static final Class<?>[] JAXB_CTX_INIT_CLASSES = new Class<?>[] {
            ContestaExpedientexLlaveRequest.class,
            ClsLlaveExpediente.class,
            mx.com.actinver.orquestador.ws.generated.ClsLlaveCampo.class,
            mx.com.actinver.orquestador.ws.usuarios.IDTicket.class
    };
    private static volatile JAXBContext JAXB_CTX;

    private static synchronized void ensureJaxbContextInitialized() {
        if (JAXB_CTX == null) {
            try {
                JAXB_CTX = JAXBContext.newInstance(JAXB_CTX_INIT_CLASSES);
                LOG.info("JAXBContext inicializado con clases: {}", (Object) JAXB_CTX_INIT_CLASSES);
            } catch (Exception e) {
                throw new RuntimeException("No se pudo crear JAXBContext", e);
            }
        }
    }

    /* ------------------------------------------------------------------  WSDL  */
    /** Sirve  GET /WSImagenes?wsdl  ──>  WsImagenes.wsdl  */
    @GetMapping(params = "wsdl", produces = MediaType.TEXT_XML_VALUE)
    public ResponseEntity<Resource> wsdl() throws IOException {

        Resource wsdl = new ClassPathResource("wsdl/WsImagenes.wsdl");

        if (!wsdl.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.TEXT_XML);
        // caché desactivada (útil en desarrollo)
        h.setCacheControl(CacheControl.noStore().mustRevalidate());
        h.setPragma("no-cache");

        // Spring enviará el Resource “streaming”, sin cargarlo completo en memoria
        return new ResponseEntity<>(wsdl, h, HttpStatus.OK);
    }

    @PostMapping(consumes = "text/xml", produces = "text/xml")
    public ResponseEntity<String> handle(@RequestBody String rawXml) throws Exception {

        String op = SoapUtils.getOperationLocalPart(rawXml);
        LOG.info("Operación SOAP recibida: {}", op);

        // Decidir destino (bypass vs local) usando el router y, si aplica, parseando la llave
        Decision decision;
        ClsLlaveExpediente llaveExp = null;
        if ("ContestaExpedientexLlave".equals(op)) {
            try {
                LOG.debug("ContestaExpedientexLlave (rawXml start):\n{}", rawXml.length() > 2000 ? rawXml.substring(0, 2000) + "...(truncated)" : rawXml);
                ContestaExpedientexLlaveRequest requestObj = (ContestaExpedientexLlaveRequest) unmarshalRequestFromXml(rawXml);
                llaveExp = requestObj != null ? requestObj.getLlave() : null;
                LOG.info("llaveExp: {}", llaveExp);
            } catch (Exception e) {
                LOG.error("Error unmarshalling ContestaExpedientexLlaveRequest", e);
            }
        }

        LOG.info("migrationCutoverDate: {}", migrationCutoverDate);
        ChronoLocalDate corteHistorico = parseCutoverDateOrDefault(migrationCutoverDate);
        LOG.info("corteHistorico: {}", corteHistorico);

        decision = router.decide(op, llaveExp, corteHistorico);

        this.client = new PassthroughSoapClient(idPortalUrl.toString());
        LOG.info("decision: {}", decision);
        // Ejecutar según la decisión
        if (decision == Decision.MODERN) {
            LOG.info("peticion a Interna: ");
            // Procesamiento interno (consulta moderna)
            String respuestaXml = procesarInternamente(op, rawXml);
            LOG.info("Respuesta interna generada para {}: {}", op, respuestaXml);
            return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(respuestaXml);

        } else {
            // Reenvío íntegro al sistema legacy (consulta histórica)
            try {
                LOG.info("peticion a idPortal: {}", idPortalUrl);
                String rawOut = client.invokeRaw(rawXml);
                return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(rawOut);
            } catch (IOException io) {
                // Error comunicando con legacy: devolver Fault SOAP
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.TEXT_XML)
                        .body(buildSoapFault(io.getMessage()));
            }
        }
    }

    /**
     * Unmarshal robusto: parsea el documento como DOM (namespace-aware), busca el nodo
     * ContestaExpedientexLlave en el namespace esperado y unmarshallea desde ese Node.
     * Devuelve null si no encuentra el nodo.
     */
    private Object unmarshalRequestFromXml(String rawXml) throws Exception {
        if (rawXml == null) throw new IllegalArgumentException("rawXml es null");

        ensureJaxbContextInitialized();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));

        // buscar por namespace y localName
        final String targetNS = "http://Digipro.servicios/WsImagenes/WsImagenes";
        final String localName = "ContestaExpedientexLlave";

        NodeList nodes = doc.getElementsByTagNameNS(targetNS, localName);
        if (nodes != null && nodes.getLength() > 0) {
            Node node = nodes.item(0);
            Unmarshaller um = JAXB_CTX.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<ContestaExpedientexLlaveRequest> j =
                    (JAXBElement<ContestaExpedientexLlaveRequest>) um.unmarshal(new DOMSource(node), ContestaExpedientexLlaveRequest.class);
            return j.getValue();
        }

        // fallback: buscar por localName sin namespace (más permisivo)
        nodes = doc.getElementsByTagName(localName);
        if (nodes != null && nodes.getLength() > 0) {
            Node node = nodes.item(0);
            Unmarshaller um = JAXB_CTX.createUnmarshaller();
            @SuppressWarnings("unchecked")
            JAXBElement<ContestaExpedientexLlaveRequest> j =
                    (JAXBElement<ContestaExpedientexLlaveRequest>) um.unmarshal(new DOMSource(node), ContestaExpedientexLlaveRequest.class);
            return j.getValue();
        }

        // si no encontramos nada, devolvemos null (el caller decide)
        LOG.debug("No se encontró elemento '{}' en el XML (namespace {}).", localName, targetNS);
        return null;
    }

    /**
     * @param op       Nombre de la operación (ej. ContestaExpedientexLlave)
     * @param rawSoap  XML recibido íntegro
     * @return XML a devolver al consumidor
     */
    public String procesarInternamente(String op, String rawSoap) throws Exception {
        LOG.info("procesarInternamente: {}", op);
        ContestaExpedientexLlaveRequest req = ContestaExpedientexLlaveRequest.builder(rawSoap).build();
        StreamSource dd = imagenesService.contestaExpedientexLlaveResponse(req);
        String respuestaXml = streamSourceToString(dd);
        return respuestaXml;
    }

    private String streamSourceToString(StreamSource ss) throws IOException {
        if (ss == null) return null;

        // si fue construido con Reader (ej. new StreamSource(new StringReader(...)))
        Reader r = ss.getReader();
        if (r != null) {
            try (Reader reader = r) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
                return sb.toString();
            }
        }

        // si fue construido con InputStream
        InputStream is = ss.getInputStream();
        if (is != null) {
            try (InputStream in = is; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
                return bos.toString(StandardCharsets.UTF_8.name());
            }
        }

        // fallback: no hay reader ni inputstream -> intentar fuente de sistema (no usual)
        throw new IllegalArgumentException("StreamSource no contiene Reader ni InputStream");
    }

    public String buildSoapFault(String faultString) {

        // escapamos caracteres XML básicos por seguridad (muy minimalista)
        String msg = faultString == null ? "" :
                faultString.replace("&", "&amp;")
                        .replace("<", "&lt;")
                        .replace(">", "&gt;");

        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<soap:Fault>" +
                "<faultcode>soap:Server</faultcode>" +
                "<faultstring xml:lang=\"es\">" + msg + "</faultstring>" +
                "</soap:Fault>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }

    private ChronoLocalDate parseCutoverDateOrDefault(DynamicString migrationCutoverDate) {
        if (migrationCutoverDate == null) {
            LOG.warn("migrationCutoverDate es null, usando comportamiento por defecto (legacy).");
            return LocalDate.MAX; // fuerza legacy por seguridad
        }

        String raw = migrationCutoverDate.toString();
        if (raw == null || raw.isBlank()) {
            LOG.warn("migrationCutoverDate vacío, usando comportamiento por defecto (legacy).");
            return LocalDate.MAX;
        }

        // normalizar: quitar espacios, comillas, tomar antes de 'T' si viene datetime
        raw = raw.trim().replace("\"", "");
        int tIdx = raw.indexOf('T');
        if (tIdx > -1) raw = raw.substring(0, tIdx);
        // si viene con hora separada por espacio, tomar antes del espacio
        int sp = raw.indexOf(' ');
        if (sp > -1) raw = raw.substring(0, sp);
        raw = raw.trim();

        // formatos probados (ordenado por mayor probabilidad)
        DateTimeFormatter[] fmts = new DateTimeFormatter[]{
                DateTimeFormatter.ISO_LOCAL_DATE,            // 2025-10-20
                DateTimeFormatter.ofPattern("yyyyMMdd"),    // 20251020
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),  // 20/10/2025
                DateTimeFormatter.ofPattern("d/M/yyyy"),    // 2/5/2025
                DateTimeFormatter.ofPattern("dd-MM-yyyy")   // 20-10-2025
        };

        for (DateTimeFormatter fmt : fmts) {
            try {
                LocalDate ld = LocalDate.parse(raw, fmt);
                LOG.debug("Cutover date parsed '{}' -> {}", raw, ld);
                return ld;
            } catch (DateTimeParseException e) {
                // intentar siguiente formato
            }
        }

        // si llegó aquí, no parseó: warn y fallback seguro a LocalDate.MAX (forzar legacy)
        LOG.warn("No se pudo parsear migrationCutoverDate='{}'. Asumir comportamiento legacy (fecha de corte no válida).", raw);
        return LocalDate.MAX;
    }
}
