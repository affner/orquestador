package mx.com.actinver.orquestador.ws.endpoint;

import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.ws.service.WsImagenesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.transform.stream.StreamSource;

@Endpoint
@Component
public class WsImagenesEndpoint {

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
        return imagenesService.contestaExpedientexLlaveResponse(req);
    }

    // ContestaFileHSM
    @PayloadRoot(namespace = NAMESPACE, localPart = "ContestaFileHSM")
    @ResponsePayload
    public StreamSource contestaFileHSM(@RequestPayload ContestaFileHSMRequest req) throws Exception {
        return imagenesService.contestaFileHSMResponse(req);
    }

}
