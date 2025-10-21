package mx.com.actinver.orquestador.ws.service;

import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;
import org.springframework.http.ResponseEntity;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;

import javax.xml.transform.stream.StreamSource;

public interface WsImagenesService {

    // nuevos métodos: responden ya marshalleados / listos para el endpoint
    StreamSource obtenLoginResponse(ObtenLoginRequest req) throws Exception;

    ResponseEntity<String> contestaExpedientexLlaveProcess(String rawXml, String op) throws Exception;

    StreamSource contestaExpedientexLlaveResponse(ContestaExpedientexLlaveRequest req) throws Exception;
    StreamSource contestaFileHSMResponse(ContestaFileHSMRequest req) throws Exception;

    ResponseEntity<String> bypass(String rawXml);
}
