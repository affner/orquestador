package mx.com.actinver.orquestador.ws.service;

import mx.com.actinver.orquestador.ws.generated.ArrayOfClsFileHSM;
import mx.com.actinver.orquestador.ws.generated.ClsFileHSM;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;

public interface WsImagenesService {
    IDTicket obtenLogin(String userID, String pwd, int proyectoID, String ip, String origen, Respuesta respuestaHolder);
    ArrayOfClsFileHSM contestaExpedientexLlave(IDTicket ticket, ClsLlaveExpediente llave, short proyID, short expedienteID, int tipoDocID, Respuesta respuestaHolder);
    ClsFileHSM contestaFileHSM(long docID, int proyID, long expedienteID, IDTicket ticket, Respuesta respuestaHolder);

}
