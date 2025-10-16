package mx.com.actinver.orquestador.ws.endpoint;

import mx.com.actinver.conf.DynamicString;
import mx.com.actinver.orquestador.util.DynamicProperty;
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

import java.io.IOException;

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
        this.client = new PassthroughSoapClient(idPortalUrl.toString());
        /* Descubrimos la operación (local‐part) con StAX
              → ultra-rápido, sin JAXB                           */
        String op = SoapUtils.getOperationLocalPart(rawXml);

        /* Decisión */
        BypassRouter.Decision decision = router.decide(op, rawXml);

        if (decision == BypassRouter.Decision.BYPASS) {
            /*  → reenvío íntegro al servidor legacy */
            try {
                String rawOut = client.invokeRaw(rawXml);
                return ResponseEntity.ok()
                        .contentType(MediaType.TEXT_XML)
                        .body(rawOut);
            } catch (IOException io) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .contentType(MediaType.TEXT_XML)
                        .body(buildSoapFault(io.getMessage()));
            }
        }

        /* 3) Lógica interna (sólo para operaciones modernas)  */
        String resp = procesarInternamente(op, rawXml);
        LOG.info("Respuesta: {}", resp);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_XML)
                .body(resp);
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

