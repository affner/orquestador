package mx.com.actinver.orquestador.endpoint;

import mx.com.actinver.orquestador.entity.AuditLogEntity;
import mx.com.actinver.orquestador.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.util.SoapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Controller para enrutar requests SOAP (bypass vs modern).
 * Implementación robusta de unmarshalling usando DOM + JAXB.
 */
@RestController
@RequestMapping("/WSImagenes")
public class SoapBypassEndpoint {

    private static final Logger LOG = LogManager.getLogger(SoapBypassEndpoint.class);



    @Autowired
    private WsImagenesService imagenesService;



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

    @PostMapping(
            value = "",
            consumes = { "text/xml", "application/soap+xml" },
            produces = "text/xml"
    )
    public ResponseEntity<String> handle(@RequestBody String rawXml) throws Exception {

        String op = SoapUtils.getOperationLocalPart(rawXml);
        LOG.info("Operación SOAP recibida: {}", op);

        // Decidir destino (bypass vs local) usando el router y, si aplica, parseando la llave
        if ("ContestaExpedientexLlave".equals(op)) {

            return imagenesService.contestaExpedientexLlaveProcess(rawXml, op);

        } else {

            return imagenesService.bypass(rawXml, op);
        }
    }



//    /**
//     * Unmarshal robusto: parsea el documento como DOM (namespace-aware), busca el nodo
//     * ContestaExpedientexLlave en el namespace esperado y unmarshallea desde ese Node.
//     * Devuelve null si no encuentra el nodo.
//     */
//    private Object unmarshalRequestFromXml(String rawXml) throws Exception {
//        if (rawXml == null) throw new IllegalArgumentException("rawXml es null");
//
//        ensureJaxbContextInitialized();
//
//        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//        dbf.setNamespaceAware(true);
//        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(rawXml)));
//
//        // buscar por namespace y localName
//        final String targetNS = "http://Digipro.servicios/WsImagenes/WsImagenes";
//        final String localName = "ContestaExpedientexLlave";
//
//        NodeList nodes = doc.getElementsByTagNameNS(targetNS, localName);
//        if (nodes != null && nodes.getLength() > 0) {
//            Node node = nodes.item(0);
//            Unmarshaller um = JAXB_CTX.createUnmarshaller();
//            @SuppressWarnings("unchecked")
//            JAXBElement<ContestaExpedientexLlaveRequest> j =
//                    (JAXBElement<ContestaExpedientexLlaveRequest>) um.unmarshal(new DOMSource(node), ContestaExpedientexLlaveRequest.class);
//            return j.getValue();
//        }
//
//        // fallback: buscar por localName sin namespace (más permisivo)
//        nodes = doc.getElementsByTagName(localName);
//        if (nodes != null && nodes.getLength() > 0) {
//            Node node = nodes.item(0);
//            Unmarshaller um = JAXB_CTX.createUnmarshaller();
//            @SuppressWarnings("unchecked")
//            JAXBElement<ContestaExpedientexLlaveRequest> j =
//                    (JAXBElement<ContestaExpedientexLlaveRequest>) um.unmarshal(new DOMSource(node), ContestaExpedientexLlaveRequest.class);
//            return j.getValue();
//        }
//
//        // si no encontramos nada, devolvemos null (el caller decide)
//        LOG.debug("No se encontró elemento '{}' en el XML (namespace {}).", localName, targetNS);
//        return null;
//    }






}
