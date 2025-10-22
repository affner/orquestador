package mx.com.actinver.orquestador.ws.endpoint;

import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.service.WsImagenesService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.transform.stream.StreamSource;


// este endpoint termina por ser solo un mock y se tiene que apagar
@Endpoint
@Component
public class MockWsImagenesEndpoint {
    private static final Logger LOG = LogManager.getLogger(SoapBypassEndpoint.class);

    public static final String NAMESPACE = "http://Digipro.servicios/WsImagenes/WsImagenes";

    @Autowired
    private WsImagenesService imagenesService;

    // ObtenLogin
    @PayloadRoot(namespace = NAMESPACE, localPart = "ObtenLogin")
    @ResponsePayload
    public StreamSource obtenLogin(@RequestPayload ObtenLoginRequest req) throws Exception {
        // endpoint limpio: sólo reenvía al service y devuelve la respuesta ya preparada
        return imagenesService.obtenLoginResponse(req);
    }

    // ContestaExpedientexLlave
    @PayloadRoot(namespace = NAMESPACE, localPart = "ContestaExpedientexLlave")
    @ResponsePayload
    public StreamSource contestaExpedientexLlave(@RequestPayload ContestaExpedientexLlaveRequest req) throws Exception {
        LOG.info("contestaExpedientexLlave: {}", req);

        return imagenesService.contestaExpedientexLlaveResponse(req);
    }

    // ContestaFileHSM
    @PayloadRoot(namespace = NAMESPACE, localPart = "ContestaFileHSM")
    @ResponsePayload
    public StreamSource contestaFileHSM(@RequestPayload ContestaFileHSMRequest req) throws Exception {
        return imagenesService.contestaFileHSMResponse(req);
    }

}
