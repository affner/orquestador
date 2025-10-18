package mx.com.actinver.orquestador.ws.service.impl;

import mx.com.actinver.orquestador.ws.endpoint.RawSoapHolder;
import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.ws.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.ObtenLoginResponse;
import mx.com.actinver.orquestador.ws.usuarios.RRespuesta;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;
import mx.com.actinver.orquestador.ws.util.WsImagenesPrefixMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;

@Service
public class WsImagenesServiceImpl implements WsImagenesService {
    private static final Logger LOG = LogManager.getLogger(WsImagenesServiceImpl.class);

    // ----------------- implementación existente (dominio) -----------------

    private IDTicket obtenLogin(String userID, String pwd, int proyectoID, String ip, String origen, Respuesta respuestaHolder) {
        // comportamiento demo (igual que antes)
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


    private ArrayOfClsFileHSM contestaExpedientexLlave(IDTicket ticket, ClsLlaveExpediente llave, short proyID, short expedienteID, int tipoDocID, Respuesta respuestaHolder) {
        ArrayOfClsFileHSM arr = new ArrayOfClsFileHSM();
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID(1L);
        f.setDescripcion("Documento demo");
        f.setTipoDocID(tipoDocID);
        f.setConsecutivo(1);
        f.setExt(".PDF");
        f.setArrayFile(new byte[]{1,2,3});
        arr.getClsFileHSM().add(f);

        respuestaHolder.setRespuestaID("0");
        respuestaHolder.setCategoria("4000");
        respuestaHolder.setDescripcionRespuesta("Expediente obtenido correctamente");
        return arr;
    }


    private ClsFileHSM contestaFileHSM(long docID, int proyID, long expedienteID, IDTicket ticket, Respuesta respuestaHolder) {
        ClsFileHSM f = new ClsFileHSM();
        f.setDocID(docID);
        f.setDescripcion("Contenido binario demo");
        f.setArrayFile(new byte[]{0,1,2,3});
        respuestaHolder.setRespuestaID("0");
        respuestaHolder.setCategoria("4000");
        respuestaHolder.setDescripcionRespuesta("OK");
        return f;
    }

    // ----------------- nuevos métodos: devuelven StreamSource listos para el endpoint -----------------

    @Override
    public StreamSource obtenLoginResponse(ObtenLoginRequest req) throws Exception {
        LOG.info("[Service] ObtenLogin called - user={}, proyectoID={}", req.getUserID(), req.getProyectoID());
        LOG.debug("[Service] Raw SOAP (holder) length={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        IDTicket t = obtenLogin(req.getUserID(), req.getStrPwd(), req.getProyectoID(),
                req.getIP(), req.getStrOrigen(), r);

        ObtenLoginResponse resp = new ObtenLoginResponse();
        resp.setIdTicket(t); // si t==null, se queda null

        // poblar RRespuesta tal como antes (si quieres leer desde r puedes mapearlo;
        // para mantener comportamiento previo lo dejamos hardcodeado como el endpoint anterior)
        RRespuesta rr = new RRespuesta();
        rr.setCodigo("2002");
        rr.setMensaje("Información");
        rr.setDetalle("(2002). Usuario o contraseña incorrecta");
        resp.setRRespuesta(rr);

        JAXBContext jc = JAXBContext.newInstance(ObtenLoginResponse.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        StringWriter sw = new StringWriter();
        m.marshal(resp, sw);

        String xml = sw.toString();
        LOG.debug("[Service] ObtenLogin response XML (len={}): {}", xml.length(), xml.length() > 2000 ? xml.substring(0,2000) + "...(truncated)" : xml);

        return new StreamSource(new StringReader(xml));
    }

    @Override
    public StreamSource contestaExpedientexLlaveResponse(ContestaExpedientexLlaveRequest req) throws Exception {
        LOG.info("[Service] ContestaExpedientexLlave - proyID={}, expedID={}, tipoDocID={}",
                req.getProyID(), req.getExpedienteID(), req.getTipoDocID());
        LOG.debug("[Service] Raw SOAP len={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        ArrayOfClsFileHSM arr = contestaExpedientexLlave(req.getTicket(), req.getLlave(),
                req.getProyID().shortValue(), req.getExpedienteID().shortValue(), req.getTipoDocID(), r);

        StringWriter sw = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ArrayOfClsFileHSM.class, Respuesta.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        sw.append("<ContestaExpedientexLlaveResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">");
        StringWriter aW = new StringWriter();
        m.marshal(arr, aW);
        sw.append(aW.toString());
        StringWriter rW = new StringWriter();
        m.marshal(r, rW);
        sw.append(rW.toString());
        sw.append("</ContestaExpedientexLlaveResponse>");

        String responseXml = sw.toString();
        LOG.info("[Service] ContestaExpedientexLlave response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Service] ContestaExpedientexLlave finished - items={}", arr != null ? arr.getClsFileHSM().size() : 0);

        return new StreamSource(new StringReader(responseXml));
    }

    @Override
    public StreamSource contestaFileHSMResponse(ContestaFileHSMRequest req) throws Exception {
        LOG.info("[Service] ContestaFileHSM - docID={}, proyID={}", req.getDocID(), req.getProyID());
        LOG.debug("[Service] Raw SOAP len={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        ClsFileHSM f = contestaFileHSM(req.getDocID(), req.getProyID(), req.getDocIDPadreExp(), req.getTicket(), r);

        StringWriter sw = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ClsFileHSM.class, Respuesta.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper", new WsImagenesPrefixMapper());

        sw.append("<ContestaFileHSMResponse xmlns=\"http://Digipro.servicios/WsImagenes/WsImagenes\">");
        StringWriter fW = new StringWriter();
        m.marshal(f, fW);
        sw.append(fW.toString());
        StringWriter rW = new StringWriter();
        m.marshal(r, rW);
        sw.append(rW.toString());
        sw.append("</ContestaFileHSMResponse>");

        String responseXml = sw.toString();
        LOG.info("[Service] ContestaFileHSM response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Service] ContestaFileHSM finished - returnedDocID={}", f != null ? f.getDocID() : null);

        return new StreamSource(new StringReader(responseXml));
    }

}
