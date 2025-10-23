package mx.com.actinver.orquestador.ws.util;


import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.dto.LlaveMetadataDto;
import mx.com.actinver.orquestador.constant.Decision;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveCampo;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SoapUtils {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SoapUtils.class);

    private static volatile JAXBContext PACKAGE_CTX;
    private static final String CONTEXT_PATH =
            "mx.com.actinver.orquestador.ws.generated:mx.com.actinver.orquestador.ws.usuarios";


    // Cache de la clase de request por operación (ContestaExpedientexLlave -> Class)
    private static final Map<String, Class<?>> REQ_CLASS_BY_OP = new ConcurrentHashMap<>();


    /**
     * Devuelve el <code>localPart</code> del primer elemento dentro de &lt;soap:Body&gt;
     *  – por ejemplo «ObtenLogin», «ContestaExpedientexLlave», …
     *  Si algo falla devuelve <code>null</code>.
     */
    public static String getOperationLocalPart(String xml) {
        XMLStreamReader r = null;
        try {
            r = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(xml));

            // Avanza hasta <soap:Body>
            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT &&
                        "Body".equals(r.getLocalName())) {

                    // El siguiente START_ELEMENT es la operación
                    while (r.hasNext()) {
                        if (r.next() == XMLStreamConstants.START_ELEMENT) {
                            return r.getLocalName();   // ← ObtenLogin, etc.
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // log.debug("No se pudo parsear SOAP", ignored);
        } finally {
            if (r != null) {               // cerrar manualmente
                try { r.close(); } catch (Exception e) { /* ignore */ }
            }
        }
        return null;
    }

    /** Crea/cacha un JAXBContext con las clases relevantes (puedes centralizarlo fuera si prefieres) */
    public static JAXBContext buildContext(Class<?>... types) {
        try {
            return JAXBContext.newInstance(types);
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo crear JAXBContext", e);
        }
    }


    public <T> String genericMarshall(T data) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(data.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(data, stringWriter);
        return stringWriter.toString();

    }

    // Quita BOM y espacios molestos
    private String sanitize(String xml) {
        if (xml == null) return "";
        String s = xml;
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);
        return s.trim();
    }

    private Document parseNsAware(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Element getFirstBodyChild(Document doc) {
        String[] soapNS = {
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.w3.org/2003/05/soap-envelope"
        };
        Element body = null;
        for (String ns : soapNS) {
            NodeList nl = doc.getElementsByTagNameNS(ns, "Body");
            if (nl != null && nl.getLength() > 0) { body = (Element) nl.item(0); break; }
        }
        if (body == null) {
            NodeList nl = doc.getElementsByTagName("Body");
            if (nl != null && nl.getLength() > 0) body = (Element) nl.item(0);
        }
        if (body == null) return null;

        // primer hijo ELEMENT (omite textos/espacios)
        org.w3c.dom.Node n = body.getFirstChild();
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) n = n.getNextSibling();
        return (Element) n;
    }
    public <T> T unmarshalFromSoapBody(String rawXml, Class<T> clazz) throws Exception {
        String xml = sanitize(rawXml);
        Element payload = null;
        Exception primaryException = null;

        try {
            Document doc = parseNsAware(xml);
            payload = getFirstBodyChild(doc);
        } catch (Exception e) {
            primaryException = e;
        }

        if (payload == null) {
            payload = getFirstBodyChildSaaj(xml);
            if (payload != null) {
                LOG.debug("SOAP payload resuelto mediante fallback SAAJ (versión agnóstica)");
            }
        }

        if (payload == null) {
            if (primaryException != null) {
                throw primaryException;
            }
            return null;
        }

        return unmarshalPayload(payload, clazz);
    }

    private Element getFirstBodyChildSaaj(String xml) {
        byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
        String[][] candidates = new String[][]{
                {SOAPConstants.SOAP_1_1_PROTOCOL, "text/xml"},
                {SOAPConstants.SOAP_1_2_PROTOCOL, "application/soap+xml"}
        };

        for (String[] candidate : candidates) {
            String protocol = candidate[0];
            try {
                MessageFactory mf = MessageFactory.newInstance(protocol);
                MimeHeaders headers = new MimeHeaders();
                headers.addHeader("Content-Type", candidate[1]);
                SOAPMessage message = mf.createMessage(headers, new ByteArrayInputStream(bytes));
                SOAPBody body = message.getSOAPBody();
                if (body == null) {
                    continue;
                }
                Iterator<?> it = body.getChildElements();
                while (it.hasNext()) {
                    Object node = it.next();
                    if (node instanceof SOAPElement) {
                        SOAPElement soapElement = (SOAPElement) node;
                        if (soapElement.getNodeType() == Node.ELEMENT_NODE) {
                            return soapElement;
                        }
                    }
                }
            } catch (SOAPException | IOException ex) {
                LOG.debug("SAAJ fallback SOAP parsing failed for protocol {}: {}", protocol, ex.getMessage());
            }
        }
        return null;
    }

    private <T> T unmarshalPayload(Element payload, Class<T> clazz) throws Exception {
        JAXBContext ctx = getPackageContext(); // tu contexto por paquetes (con TCCL)
        try {
            Unmarshaller um = ctx.createUnmarshaller();
            JAXBElement<T> j = um.unmarshal(new DOMSource(payload), clazz);
            return j.getValue();
        } catch (JAXBException ex) {
            // Fallback 1: contexto per-class (no requiere ClassLoader explícito)
            try {
                JAXBContext perClass = JAXBContext.newInstance(clazz);
                Unmarshaller um2 = perClass.createUnmarshaller();
                JAXBElement<T> j2 = um2.unmarshal(new DOMSource(payload), clazz);
                return j2.getValue();
            } catch (JAXBException ex2) {
                // Fallback 2: contexto por package + TCCL (requiere ObjectFactory en el paquete)
                try {
                    String pkg = clazz.getPackage().getName();
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    JAXBContext byPkg = JAXBContext.newInstance(pkg, cl);
                    Unmarshaller um3 = byPkg.createUnmarshaller();
                    Object o = um3.unmarshal(new DOMSource(payload));
                    if (o instanceof JAXBElement) o = ((JAXBElement<?>) o).getValue();
                    return clazz.cast(o);
                } catch (JAXBException | ClassCastException ex3) {
                    // Si tampoco, re-lanza el original (más representativo del fallo real)
                    throw ex;
                }
            }
        }
    }



    public String base64ToStringConversion(String bytes) {
        return new String(Base64.decodeBase64(bytes));
    }

    public String stringToBase64Conversion(String text) {
        Base64 base64 = new Base64();
        return new String(base64.encode(text.getBytes()));

    }

    public String getTodayDatexml() {
        try {

            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            return date.toString();

        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public XMLGregorianCalendar getDatexml() throws DatatypeConfigurationException {


        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        return date;

    }



    public SOAPMessage getSoapMessageFromXml(String xml) throws SOAPException, IOException {
        MessageFactory factory = MessageFactory.newInstance();
        MimeHeaders header = new MimeHeaders();
        header.setHeader("soapAction", "operation1");
        SOAPMessage soapMessage = factory.createMessage(header, new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
        return soapMessage;

    }

    public SOAPMessage sendSoapMessage(SOAPMessage request, String url) throws SOAPException {

        SOAPConnectionFactory soapConnectionFactory = null;

        soapConnectionFactory = SOAPConnectionFactory.newInstance();

        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        SOAPMessage soapResponse = soapConnection.call(request, url);

        return soapResponse;

    }

    public String getValueFromResponseBody(SOAPMessage soapMessage, String tagName) {
        try {
            return soapMessage.getSOAPPart().getEnvelope()
                    .getBody().getElementsByTagName(tagName).item(0)
                    .getTextContent();
        } catch (Exception e) {
            return null;
        }

    }

    public Document setValueFromXml( Document doc , String name, String value) {

        doc.getDocumentElement().normalize();
        org.w3c.dom.Node nodVal = doc.getElementsByTagName(name).item(0);
        nodVal.setTextContent(value);

        return doc;
    }

    public SOAPMessage setSoapMessageFromXml(SOAPMessage request, Document xml) {

        SOAPBody soapBody = null;
        try {
            soapBody = request.getSOAPBody();
            soapBody.removeContents();
            soapBody.addDocument(xml);
            return request;
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getValueFromXml(Document doc, String tagName) {

        doc.getDocumentElement().normalize();
        org.w3c.dom.Node nodVal = doc.getElementsByTagName(tagName).item(0);
        String value = nodVal.getFirstChild().getTextContent();
        return value;
    }

    public String nodeToString(org.w3c.dom.Node node) throws Exception {
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }

    public String convertToString(SOAPMessage message) throws IOException, SOAPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        message.writeTo(out);

        String strMsg = new String(out.toByteArray());
        return strMsg;
    }

    public String streamSourceToString(StreamSource ss) throws IOException {
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


    public ChronoLocalDate parseCutoverDateOrDefault(DynamicString migrationCutoverDate) {
        if (migrationCutoverDate == null) {
            LOG.warn("migrationCutoverDate es null, usando comportamiento por defecto (legacy).");
            return LocalDate.MAX; // fuerza legacy por seguridad
        }

        String raw = migrationCutoverDate.toString();
        if (raw == null || raw.isEmpty()) {
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



    /** Resuelve la clase del request en runtime a partir del nombre de operación. */
    public static Class<?> resolveRequestClass(String operationLocalPart) {
        return REQ_CLASS_BY_OP.computeIfAbsent(operationLocalPart, op -> {
            String base = "mx.com.actinver.orquestador.ws.generated.";
            String[] candidates = new String[] {
                    base + op + "Request",  // p.ej. ContestaExpedientexLlaveRequest
                    base + op               // por si tus clases NO usan sufijo Request
            };
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (String fqcn : candidates) {
                try { return Class.forName(fqcn, true, cl); }
                catch (ClassNotFoundException ignore) {}
            }
            return null;
        });
    }

    public static JAXBContext getPackageContext() {
        if (PACKAGE_CTX == null) {
            synchronized (SoapUtils.class) {
                if (PACKAGE_CTX == null) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    try {
                        PACKAGE_CTX = JAXBContext.newInstance(CONTEXT_PATH, cl);
                    } catch (JAXBException e) {
                        throw new RuntimeException("No se pudo crear JAXBContext para: " + CONTEXT_PATH, e);
                    }
                }
            }
        }
        return PACKAGE_CTX;
    }

    public Decision decide(String operationLocalPart,
                           ClsLlaveExpediente llave,
                           ChronoLocalDate corteHistorico) {
        LOG.info("operationLocalPart: {}", operationLocalPart);
        LOG.info("llave: {}", llave);

        if (llave == null || llave.getCampos() == null) {
            return Decision.LEGACY;
        }

        // ObtenLogin -> como lo tienes hoy
        if ("ObtenLogin".equals(operationLocalPart)) {
            return Decision.LEGACY;
        }

        if ("ContestaExpedientexLlave".equals(operationLocalPart)) {
            LlaveMetadataDto llaveData = extraerLlaveMetadata(llave);
            Integer month = llaveData != null ? llaveData.getMonth() : null;   // 1..12
            Integer year = llaveData != null ? llaveData.getYear() : null;   // 4 dígitos
            LOG.info("periodo: month={}, year={}", month, year);

            // si no hay periodo, tratamos como histórico
            if (month == null || year == null) {
                LOG.info("periodo nulo: month={}, year={}", month, year);
                return Decision.LEGACY;
            }
            if (month < 1 || month > 12) {
                LOG.info("periodo invalido: month={}, year={}", month, year);
                return Decision.LEGACY;
            }

            // Periodo de la llave como YearMonth
            YearMonth periodoYM;
            try {
                periodoYM = YearMonth.of(year, month);
            } catch (RuntimeException ex) { // por si llega año inválido
                LOG.warn("Periodo inválido en llave: y={}, m={}. {}", year, month, ex.toString());
                return Decision.LEGACY;
            }

            // Fecha de corte -> YearMonth (si es nula, forzamos legacy por seguridad)
            if (corteHistorico == null) {
                LOG.warn("corteHistorico es null -> LEGACY");
                return Decision.LEGACY;
            }
            LocalDate corteLD;
            try {
                corteLD = LocalDate.from(corteHistorico);
            } catch (RuntimeException ex) {
                LOG.warn("No se pudo convertir corteHistorico a LocalDate: {} -> LEGACY", ex.toString());
                return Decision.LEGACY;
            }
            YearMonth corteYM = YearMonth.of(corteLD.getYear(), corteLD.getMonth());

            // si periodo < corte -> LEGACY, si periodo >= corte -> MODERN
            boolean moderno = !periodoYM.isBefore(corteYM);
            LOG.info("Comparación YearMonth - periodo={} corte={} => {}", periodoYM, corteYM,
                    moderno ? "MODERN" : "LEGACY");
            return moderno ? Decision.MODERN : Decision.LEGACY;
        }

        LOG.info("Otra Operacion -> LEGACY");
        return Decision.LEGACY;
    }
    public LlaveMetadataDto extraerLlaveMetadata(ClsLlaveExpediente llave) {
        if (llave == null || llave.getCampos() == null) return null;

        LOG.info("llave: {}", llave);
        int idx = 0;
        for (ClsLlaveCampo campo : llave.getCampos()) {
            LOG.info("campo[{}]: {}", idx++, campo);
            if (campo == null) continue;

            String nombre = campo.getCampo();
            String valor = campo.getValor();

            LOG.info("campo.nombre='{}' campo.valor='{}'", nombre, valor);

            // Según tu XML actual el campo de interés es "Llave"
            if (nombre != null && "Llave".equalsIgnoreCase(nombre.trim())) {
                if (valor == null || valor.trim().isEmpty()) {
                    LOG.warn("Campo 'Llave' presente pero vacío");
                    return null;
                }
                try {
                    LlaveMetadataDto metadata = parseLlaveFlexible(valor.trim());
                    LOG.info("Llave parseada correctamente -> periodo={}, negocio={}, contrato={}",
                            metadata.getMonth() +""+ metadata.getYear(), metadata.getNegocio(), metadata.getContrato());
                    return metadata;
                } catch (Exception e) {
                    LOG.error("extraerLlaveMetadata excepcion: {}______{}", e.getMessage(), e);
                    return null;
                }
            }
        }


        LOG.error("No se encontró campo 'Llave' en la llave del expediente");
        return null;
    }
    private static LlaveMetadataDto parseLlaveFlexible(String llave) {
        Objects.requireNonNull(llave, "llave requerida");
        String digits = llave.replaceAll("\\D", "");
        if (digits.length() < 8) {
            throw new IllegalArgumentException("Llave inválida o muy corta: " + llave);
        }

        if (digits.length() >= 8) {
            try {
                int mes2 = Integer.parseInt(digits.substring(0, 2));
                int anio4 = Integer.parseInt(digits.substring(2, 6));
                if (isValidMonth(mes2) && isValidYear(anio4)) {
                    String negocio = sub(digits, 6, 8);
                    String contrato = sub(digits, 8, digits.length());
                    if (!negocio.isEmpty() && !contrato.isEmpty()) {

                        return new LlaveMetadataDto(anio4, mes2, Long.parseLong(negocio), Integer.parseInt(contrato));
                    }
                }
            } catch (Exception ignored) {
                // continuar con el siguiente intento
            }
        }

        try {
            int mes1 = Integer.parseInt(digits.substring(0, 1));
            int anio4 = Integer.parseInt(digits.substring(1, 5));
            if (!isValidMonth(mes1) || !isValidYear(anio4)) {
                throw new IllegalArgumentException("Mes/Año fuera de rango en llave: " + llave);
            }
            String negocio = sub(digits, 5, 7);
            String contrato = sub(digits, 7, digits.length());
            if (negocio.isEmpty() || contrato.isEmpty()) {
                throw new IllegalArgumentException("Negocio/Contrato incompletos en llave: " + llave);
            }
            return new LlaveMetadataDto(anio4, mes1, Long.parseLong(negocio), Integer.parseInt(contrato));
        } catch (Exception ex) {
            throw new IllegalArgumentException("No se pudo parsear la llave: " + llave, ex);
        }
    }

    /**
     * Extrae la fecha de periodo (tipo LocalDate) de la llave del expediente.
     * la llave comienza con mes (1 o 2 dígitos) seguido por año (4 dígitos).
     * Si no puede parsearse, devuelve null.
     */
    public LlaveMetadataDto extraerLlaveMetadata(ClsLlaveExpediente llave, int tipoDocID) {
        if (llave == null || llave.getCampos() == null) return null;

        LOG.info("llave: {}", llave);
        int idx = 0;
        for (ClsLlaveCampo campo : llave.getCampos()) {
            LOG.info("campo[{}]: {}", idx++, campo);
            if (campo == null) continue;

            String nombre = campo.getCampo();
            String valor = campo.getValor();

            LOG.info("campo.nombre='{}' campo.valor='{}'", nombre, valor);

            // Según tu XML actual el campo de interés es "Llave"
            if (nombre != null && "Llave".equalsIgnoreCase(nombre.trim())) {
                if (valor == null || valor.trim().isEmpty()) {
                    LOG.warn("Campo 'Llave' presente pero vacío");
                    return null;
                }
                try {
                    LlaveMetadataDto metadata = parseLlaveFlexible(valor.trim(), tipoDocID);
                    LOG.info("Llave parseada correctamente -> periodo={}, negocio={}, contrato={}",
                            metadata.getMonth() +""+ metadata.getYear(), metadata.getNegocio(), metadata.getContrato());
                    return metadata;
                } catch (Exception e) {
                    LOG.error("extraerLlaveMetadata excepcion: {}______{}", e.getMessage(), e);
                    return null;
                }
            }
        }


        LOG.error("No se encontró campo 'Llave' en la llave del expediente");
        return null;
    }

    public Integer extraerTipoDato(ClsLlaveExpediente llave) {
        if (llave == null || llave.getCampos() == null) {
            return null;
        }

        Integer fallback = null;

        for (ClsLlaveCampo campo : llave.getCampos()) {
            if (campo == null) {
                continue;
            }

            String campoNombre = campo.getCampo();
            String tipoDato = campo.getTipoDato();

            if (StringUtils.hasText(tipoDato)) {
                try {
                    Integer parsed = Integer.valueOf(tipoDato.trim());
                    if (StringUtils.hasText(campoNombre) && "Llave".equalsIgnoreCase(campoNombre.trim())) {
                        return parsed;
                    }
                    if (fallback == null) {
                        fallback = parsed;
                    }
                } catch (NumberFormatException ex) {
                    LOG.debug("TipoDato no numérico en campo {}: {}", campoNombre, tipoDato);
                }
            }

            if (StringUtils.hasText(campoNombre) && "TipoDato".equalsIgnoreCase(campoNombre.trim())) {
                String valor = campo.getValor();
                if (StringUtils.hasText(valor)) {
                    try {
                        return Integer.valueOf(valor.trim());
                    } catch (NumberFormatException ex) {
                        LOG.debug("Valor de TipoDato no numérico: {}", valor);
                    }
                }
            }
        }

        return fallback;
    }

    private static LlaveMetadataDto parseLlaveFlexible(String llave, int tipoDocID) {
        Objects.requireNonNull(llave, "llave requerida");
        String digits = llave.replaceAll("\\D", "");
        if (digits.length() < 7) { // caso mínimo: MYYYY + negocio(2) + al menos 0 de contrato
            throw new IllegalArgumentException("Llave inválida o muy corta: " + llave);
        }

        // Intento 1: MMYYYY...
        int mLen = 0;
        int mes, anio;
        try {
            int mm = Integer.parseInt(digits.substring(0, Math.min(2, digits.length())));
            int yyyy = Integer.parseInt(digits.substring(2, Math.min(6, digits.length())));
            if (isValidMonth(mm) && isValidYear(yyyy)) {
                mes = mm; anio = yyyy; mLen = 2; // mes ocupa 2 dígitos
            } else {
                throw new NumberFormatException("no MMYYYY");
            }
        } catch (Exception ignore) {
            // Intento 2: MYYYY...
            if (digits.length() < 6) {
                throw new IllegalArgumentException("Llave no contiene periodo válido (MYYYY/MMYYYY): " + llave);
            }
            int m = Integer.parseInt(digits.substring(0, 1));
            int yyyy = Integer.parseInt(digits.substring(1, 5));
            if (!isValidMonth(m) || !isValidYear(yyyy)) {
                throw new IllegalArgumentException("Periodo inválido en llave: " + llave);
            }
            mes = m; anio = yyyy; mLen = 1; // mes ocupa 1 dígito
        }

        // Después del periodo viene negocio (2 dígitos) y luego contrato
        int idxAfterPeriod = mLen + 4; // mes (1 o 2) + año (4)
        if (digits.length() < idxAfterPeriod + 2) {
            throw new IllegalArgumentException("Llave sin negocio de 2 dígitos: " + llave);
        }
        String negocioRaw = digits.substring(idxAfterPeriod, idxAfterPeriod + 2);
        String contrato = (digits.length() > idxAfterPeriod + 2)
                ? digits.substring(idxAfterPeriod + 2)
                : "";

        if (contrato.isEmpty()) {
            throw new IllegalArgumentException("Llave sin contrato: " + llave);
        }

        // Regla de mapeo negocio
        String negocioInt;
        switch (negocioRaw) {
            case "01": // banco… excepto tipoDocID == 3
                negocioInt = (tipoDocID == 3) ? "31" : "10"; // 31=Crédito, 10=Banco
                break;
            case "02": // casa
                negocioInt = "11";
                break;
            default:
                throw new IllegalArgumentException("Negocio desconocido en llave: " + negocioRaw);
        }


        return new LlaveMetadataDto(anio, mes, Long.parseLong(negocioInt), Integer.parseInt(contrato));
    }


    private static String sub(String value, int start, int end) {
        if (value == null) {
            return "";
        }
        if (start >= value.length()) {
            return "";
        }
        int safeEnd = Math.min(value.length(), Math.max(start, end));
        if (safeEnd <= start) {
            return "";
        }
        return value.substring(start, safeEnd);
    }

    private static boolean isValidMonth(int month) {
        return month >= 1 && month <= 12;
    }

    private static boolean isValidYear(int year) {
        return year >= 1900 && year <= 2100;
    }



}
