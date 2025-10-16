package mx.com.actinver.orquestador.ws.service.impl;

import mx.com.actinver.orquestador.ws.generated.ArrayOfClsFileHSM;
import mx.com.actinver.orquestador.ws.generated.ClsFileHSM;
import mx.com.actinver.orquestador.ws.generated.ClsLlaveExpediente;
import mx.com.actinver.orquestador.ws.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

@Service
public class WsImagenesServiceImpl implements WsImagenesService {
    private static final Logger LOG = LogManager.getLogger(WsImagenesServiceImpl.class);

  //  @DynamicProperty("${id-portal.soap.url}")
//    @Value("${id-portal.soap.url}")
//    private String forwardUrl;

    @Override
    public IDTicket obtenLogin(String userID, String pwd, int proyectoID, String ip, String origen, Respuesta respuestaHolder) {

        // *************************************************************
        // BYPASS HOOK (VISIBILIDAD): aquí es donde se implementará el reenvío
        // Si quieres reenviar el SOAP EXACTO que llegó al endpoint, puedes:
        //   String rawSoap = RawSoapHolder.get();
        //   // forwardRawSoap(rawSoap) -> llamar a RestTemplate/HTTP client que haga POST al destino
        //   // parsear la respuesta remota y poblar IDTicket + Respuesta
        // Por ahora dejamos el comportamiento hardcodeado (demo) tal como pediste.
        // *************************************************************

        // ejemplo (comentado) de uso:
    /*
    String rawSoap = RawSoapHolder.get();
    if (rawSoap != null && !rawSoap.isEmpty() && forwardUrl != null && !forwardUrl.isEmpty()) {
        // Implementación futura: reenviar rawSoap a forwardUrl y parsear la respuesta.
        // String remoteResponse = forwardRawSoap(rawSoap);
        // parse remoteResponse -> poblar respuestaHolder y devolver IDTicket parseado
    }
    */
        // validación simple
        if ("demo".equals(userID) && "demo".equals(pwd)) {
            IDTicket t = new IDTicket();
            t.setTicketID("TICKET-12345");
            t.setUsrID(userID);
            t.setIp(ip);
            t.setProyectoID(proyectoID);
            try {
                XMLGregorianCalendar x = DatatypeFactory.newInstance()
                        .newXMLGregorianCalendar(new GregorianCalendar());
                t.setFechaExpiracion(x);
            } catch (Exception ignored) {}
            respuestaHolder.setRespuestaID("0");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("OK");
            return t;
        } else {
            respuestaHolder.setRespuestaID("2002");
            respuestaHolder.setCategoria("4000");
            respuestaHolder.setDescripcionRespuesta("Usuario o contraseña incorrecta");
            return null;
        }
    }

    @Override
    public ArrayOfClsFileHSM contestaExpedientexLlave(IDTicket ticket, ClsLlaveExpediente llave, short proyID, short expedienteID, int tipoDocID, Respuesta respuestaHolder) {
        // respuesta stub: devolver lista con un ClsFileHSM
        ArrayOfClsFileHSM arr = new ArrayOfClsFileHSM();
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID(1L);
        f.setDescripcion("Documento demo");
        f.setTipoDocID(tipoDocID);
        f.setConsecutivo(1);
        f.setExt(".PDF");
        f.setArrayFile(new byte[]{1,2,3}); // demo
        arr.getClsFileHSM().add(f);

        respuestaHolder.setRespuestaID("0");
        respuestaHolder.setCategoria("4000");
        respuestaHolder.setDescripcionRespuesta("Expediente obtenido correctamente");
        return arr;
    }

    @Override
    public ClsFileHSM contestaFileHSM(long docID, int proyID, long expedienteID, IDTicket ticket, Respuesta respuestaHolder) {
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID(docID);
        f.setDescripcion("Contenido binario demo");
        f.setArrayFile(new byte[]{0,1,2,3});
        respuestaHolder.setRespuestaID("0");
        respuestaHolder.setCategoria("4000");
        respuestaHolder.setDescripcionRespuesta("OK");
        return f;
    }
}
