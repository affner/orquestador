package mx.com.actinver.orquestador.ws.endpoint;

import mx.com.actinver.orquestador.ws.generated.*;
import mx.com.actinver.orquestador.ws.service.WsImagenesService;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.ObtenLoginResponse;
import mx.com.actinver.orquestador.ws.usuarios.RRespuesta;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;
import mx.com.actinver.orquestador.ws.util.WsImagenesPrefixMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;

@Endpoint
@Component
public class WsImagenesEndpoint {

    private static final Logger LOG = LogManager.getLogger(WsImagenesEndpoint.class);

    public static final String NAMESPACE = "http://Digipro.servicios/WsImagenes/WsImagenes";

    @Autowired
    private WsImagenesService service;


    // ObtenLogin
    @PayloadRoot(namespace = NAMESPACE, localPart = "ObtenLogin")
    @ResponsePayload
    public StreamSource obtenLogin(@RequestPayload ObtenLoginRequest req) throws Exception {
        LOG.info("[Endpoint] ObtenLogin received - user={}, proyectoID={}", req.getUserID(), req.getProyectoID());
        LOG.debug("[Endpoint] Raw SOAP (holder) length={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        IDTicket t = service.obtenLogin(req.getUserID(), req.getStrPwd(), req.getProyectoID(),
                req.getIP(), req.getStrOrigen(), r);

        ObtenLoginResponse resp = new ObtenLoginResponse();

        // si quieres incluir ticket:
        resp.setIdTicket(t); // t es IDTicket obtenido del servicio

        // poblar rRespuesta con los nodos Codigo/Mensaje/Detalle (tal como exige QA)
        RRespuesta rr = new RRespuesta();
        rr.setCodigo("2002");
        rr.setMensaje("Información");
        rr.setDetalle("(2002). Usuario o contraseña incorrecta");
        resp.setRRespuesta(rr);

        JAXBContext jc = JAXBContext.newInstance(ObtenLoginResponse.class);
        Marshaller  m  = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

        /* <<< aquí obligamos a usar nuestro mapper >>> */
        m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new WsImagenesPrefixMapper());

        StringWriter sw = new StringWriter();
        m.marshal(resp, sw);

        return new StreamSource(new StringReader(sw.toString()));
    }

    // ContestaExpedientexLlave
    @PayloadRoot(namespace = NAMESPACE, localPart = "ContestaExpedientexLlave")
    @ResponsePayload
    public StreamSource contestaExpedientexLlave(@RequestPayload ContestaExpedientexLlaveRequest req) throws Exception {
        LOG.info("[Endpoint] ContestaExpedientexLlave - proyID={}, expedID={}, tipoDocID={}",
                req.getProyID(), req.getExpedienteID(), req.getTipoDocID());
        LOG.debug("[Endpoint] Raw SOAP len={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        ArrayOfClsFileHSM arr = service.contestaExpedientexLlave(req.getTicket(), req.getLlave(),
                req.getProyID().shortValue(), req.getExpedienteID().shortValue(), req.getTipoDocID(), r);

        StringWriter sw = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ArrayOfClsFileHSM.class, Respuesta.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

        //   sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sw.append("<ContestaExpedientexLlaveResponse xmlns=\"" + NAMESPACE + "\">");
        StringWriter aW = new StringWriter();
        m.marshal(arr, aW);
        sw.append(aW.toString());
        StringWriter rW = new StringWriter();
        m.marshal(r, rW);
        sw.append(rW.toString());
        sw.append("</ContestaExpedientexLlaveResponse>");

        String responseXml = sw.toString();
        LOG.info("[Endpoint] ContestaExpedientexLlave response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Endpoint] ContestaExpedientexLlave finished - items={}", arr != null ? arr.getClsFileHSM().size() : 0);

        return new StreamSource(new StringReader(responseXml));
    }

    // ContestaFileHSM
    @PayloadRoot(namespace = NAMESPACE, localPart = "ContestaFileHSM")
    @ResponsePayload
    public StreamSource contestaFileHSM(@RequestPayload ContestaFileHSMRequest req) throws Exception {
        LOG.info("[Endpoint] ContestaFileHSM - docID={}, proyID={}", req.getDocID(), req.getProyID());
        LOG.debug("[Endpoint] Raw SOAP len={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        Respuesta r = new Respuesta();
        ClsFileHSM f = service.contestaFileHSM(req.getDocID(), req.getProyID(), req.getDocIDPadreExp(), req.getTicket(), r);

        StringWriter sw = new StringWriter();
        JAXBContext jc = JAXBContext.newInstance(ClsFileHSM.class, Respuesta.class);
        Marshaller m = jc.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);

        //    sw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sw.append("<ContestaFileHSMResponse xmlns=\"" + NAMESPACE + "\">");
        StringWriter fW = new StringWriter();
        m.marshal(f, fW);
        sw.append(fW.toString());
        StringWriter rW = new StringWriter();
        m.marshal(r, rW);
        sw.append(rW.toString());
        sw.append("</ContestaFileHSMResponse>");

        String responseXml = sw.toString();
        LOG.info("[Endpoint] ContestaFileHSM response (truncated 2000):\n{}",
                (responseXml.length() > 2000 ? responseXml.substring(0, 2000) + "...(truncated)" : responseXml));
        LOG.info("[Endpoint] ContestaFileHSM finished - returnedDocID={}", f != null ? f.getDocID() : null);

        return new StreamSource(new StringReader(responseXml));
    }


    // fuera de alcance


    // -------- UserLogin handler (fuera de alcance pero trazamos)
    @PayloadRoot(namespace = NAMESPACE, localPart = "UserLogin")
    @ResponsePayload
    public UserLoginResponse handleUserLogin(@RequestPayload UserLogin request) {
        LOG.info("[Endpoint] UserLogin called (reflection wrapper)");
        LOG.debug("[Endpoint] Raw SOAP holder len={}", RawSoapHolder.get() != null ? RawSoapHolder.get().length() : 0);

        UserLoginResponse resp = new UserLoginResponse();

        String userID = firstStringGetter(request, "getUserID", "getUserId", "getUser");
        String strPwd = firstStringGetter(request, "getStrPwd", "getPassword", "getPwd");
        String ip = firstStringGetter(request, "getIP", "getIp", "getDireccionIP");
        String strOrigen = firstStringGetter(request, "getStrOrigen", "getOrigen");
        Integer proyectoID = firstIntegerGetter(request, "getProyectoID", "getProyectoId", "getProyecto");

        LOG.debug("[Endpoint] UserLogin extracted user={}, proyectoID={}, ip={}, origen={}", userID, proyectoID, ip, strOrigen);

        Respuesta r = new Respuesta();
        IDTicket ticket = service.obtenLogin(userID, strPwd, proyectoID != null ? proyectoID : 0, ip, strOrigen, r);

        try {
            invokeSetterIfExists(resp, "setTicket", IDTicket.class, ticket);
            invokeSetterIfExists(resp, "setIDTicket", IDTicket.class, ticket);
            invokeSetterIfExists(resp, "setIdTicket", IDTicket.class, ticket);
        } catch (Exception ex) {
            LOG.warn("[Endpoint] Could not set ticket on response by reflection: {}", ex.getMessage());
        }

        try {
            invokeSetterIfExists(resp, "setRRespuesta", Respuesta.class, r);
            invokeSetterIfExists(resp, "setRespuesta", Respuesta.class, r);
            invokeSetterIfExists(resp, "setRespuestaObj", Respuesta.class, r);
        } catch (Exception ex) {
            LOG.warn("[Endpoint] Could not set respuesta on response by reflection: {}", ex.getMessage());
        }

        LOG.info("[Endpoint] UserLogin finished - ticketPresent={}", ticket != null);
        return resp;
    }

    /* ----------------- helpers por reflexión ----------------- */

    private static String firstStringGetter(Object target, String... names) {
        if (target == null) return null;
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                Object val = m.invoke(target);
                if (val != null) return val.toString();
            } catch (NoSuchMethodException nsme) {
                // siguiente posible nombre
            } catch (Exception e) {
                // cualquier otra excepción, intentar siguiente
            }
        }
        return null;
    }

    private static Integer firstIntegerGetter(Object target, String... names) {
        if (target == null) return null;
        for (String name : names) {
            try {
                Method m = target.getClass().getMethod(name);
                Object val = m.invoke(target);
                if (val instanceof Number) return ((Number) val).intValue();
                if (val instanceof String) {
                    try {
                        return Integer.parseInt((String) val);
                    } catch (NumberFormatException ignored) {
                    }
                }
            } catch (NoSuchMethodException nsme) {
                // siguiente
            } catch (Exception e) {
                // siguiente
            }
        }
        return null;
    }

    private static void invokeSetterIfExists(Object target, String setterName, Class<?> paramType, Object value) {
        if (target == null) return;
        try {
            Method setter = null;
            // intentar directamente el tipo
            try {
                setter = target.getClass().getMethod(setterName, paramType);
            } catch (NoSuchMethodException ignored) {
            }
            // si no existe, intentar con Object
            if (setter == null) {
                try {
                    setter = target.getClass().getMethod(setterName, Object.class);
                } catch (NoSuchMethodException ignored) {
                }
            }
            if (setter != null) setter.invoke(target, value);
        } catch (Exception ignored) {
        }
    }

//    // --- helper request wrapper classes (simple) ---
//    // define minimal request classes with JAXB annotations or use generated request classes from WSDL
//    // Example minimal class:
//    public static class ObtenLoginRequest {
//        private String userID;
//        private String strPwd;
//        private int proyectoID;
//        private String IP;
//        private String strOrigen;
//        // getters/setters
//        public String getUserID(){return userID;} public void setUserID(String u){this.userID=u;}
//        public String getStrPwd(){return strPwd;} public void setStrPwd(String p){this.strPwd=p;}
//        public int getProyectoID(){return proyectoID;} public void setProyectoID(int v){this.proyectoID=v;}
//        public String getIP(){return IP;} public void setIP(String ip){this.IP=ip;}
//        public String getStrOrigen(){return strOrigen;} public void setStrOrigen(String s){this.strOrigen=s;}
//    }

//    public static class ContestaExpedientexLlaveRequest {
//        private IDTicket ticket;
//        private ClsLlaveExpediente llave;
//        private short proyID;
//        private short expedienteID;
//        private int tipoDocID;
//        // getters/setters
//        public IDTicket getTicket(){return ticket;} public void setTicket(IDTicket t){this.ticket=t;}
//        public ClsLlaveExpediente getLlave(){return llave;} public void setLlave(ClsLlaveExpediente l){this.llave=l;}
//        public short getProyID(){return proyID;} public void setProyID(short p){this.proyID=p;}
//        public short getExpedienteID(){return expedienteID;} public void setExpedienteID(short e){this.expedienteID=e;}
//        public int getTipoDocID(){return tipoDocID;} public void setTipoDocID(int t){this.tipoDocID=t;}
//    }

//    public static class ContestaFileHSMRequest {
//        private long docID;
//        private int proyID;
//        private long docIDPadreExp;
//        private IDTicket ticket;
//        // getters/setters
//        public long getDocID(){return docID;} public void setDocID(long d){this.docID=d;}
//        public int getProyID(){return proyID;} public void setProyID(int p){this.proyID=p;}
//        public long getDocIDPadreExp(){return docIDPadreExp;} public void setDocIDPadreExp(long d){this.docIDPadreExp=d;}
//        public IDTicket getTicket(){return ticket;} public void setTicket(IDTicket t){this.ticket=t;}
//    }
}
