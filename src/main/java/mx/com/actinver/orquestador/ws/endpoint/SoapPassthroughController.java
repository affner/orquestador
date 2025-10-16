package mx.com.actinver.orquestador.ws.endpoint;

import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.util.DynamicProperty;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import mx.com.actinver.orquestador.ws.generated.ContestaExpedientexLlaveRequest;
import mx.com.actinver.orquestador.ws.proxy.PassthroughSoapClient;
import mx.com.actinver.orquestador.ws.util.BypassRouter;
import mx.com.actinver.orquestador.ws.util.SoapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

@RestController
@RequestMapping("/WSImagenes")
public class SoapPassthroughController {


    private static final Logger LOG = LogManager.getLogger(SoapPassthroughController.class);

    private PassthroughSoapClient client;

    @Autowired
    private  BypassRouter router;

    @DynamicProperty("${id-portal.url}")
    private DynamicString idPortalUrl;
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

    @PostMapping(consumes="text/xml", produces="text/xml")
    public ResponseEntity<String> handle(@RequestBody String rawXml) {
        LOG.info("peticion a idPortal: {}", idPortalUrl);
        String op = SoapUtils.getOperationLocalPart(rawXml);
        LOG.info("Operación SOAP recibida: {}", op);

        // Decidir destino (bypass vs local) usando el router y, si aplica, parseando la llave
        BypassRouter.Decision decision;
        ClsLlaveExpediente llaveExp = null;
        if ("ContestaExpedientexLlave".equals(op)) {
            try {
                JAXBContext ctx = JAXBContext.newInstance(ContestaExpedientexLlaveRequest.class);
                Unmarshaller um = ctx.createUnmarshaller();
                JAXBElement<ContestaExpedientexLlaveRequest> reqElem =
                        um.unmarshal(new StreamSource(new StringReader(rawXml)), ContestaExpedientexLlaveRequest.class);
                ContestaExpedientexLlaveRequest requestObj = reqElem.getValue();
                llaveExp = requestObj != null ? requestObj.getLlave() : null;
            } catch (Exception e) {
                LOG.error("Error unmarshalling ContestaExpedientexLlaveRequest", e);
            }
        }

        decision = router.decide(op, llaveExp);
        this.client = new PassthroughSoapClient(idPortalUrl.toString());

        // Ejecutar según la decisión
        if (decision == BypassRouter.Decision.BYPASS) {
            // Reenvío íntegro al sistema legacy (consulta histórica)
            try {
                String rawOut = client.invokeRaw(rawXml);
                return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(rawOut);
            } catch (IOException io) {
                // Error comunicando con legacy: devolver Fault SOAP
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.TEXT_XML)
                        .body(buildSoapFault(io.getMessage()));
            }
        } else {
            // Procesamiento interno (consulta moderna)
            String respuestaXml = procesarInternamente(op, rawXml);
            LOG.info("Respuesta interna generada para {}: {}", op, respuestaXml);
            return ResponseEntity.ok().contentType(MediaType.TEXT_XML).body(respuestaXml);
        }
    }

    /**
     * @param op       Nombre de la operación (ej. ContestaExpedientexLlave)
     * @param rawSoap  XML recibido íntegro
     * @return         XML a devolver al consumidor
     */
    public static String procesarInternamente(String op, String rawSoap) {

        // --- Ejemplo 100 % stub ---
        return "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<soap:Body>" +
                "<soap:Fault>" +
                "<faultcode>soap:Client</faultcode>" +
                "<faultstring xml:lang=\"es\">" +
                "Operación '" + op + "' procesada internamente (stub)" +
                "</faultstring>" +
                "</soap:Fault>" +
                "</soap:Body>" +
                "</soap:Envelope>";
    }
    public  String buildSoapFault(String faultString) {

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
}

