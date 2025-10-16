package mx.com.actinver.orquestador.ws.generated;

import mx.com.actinver.orquestador.ws.usuarios.Respuesta;

import javax.xml.bind.annotation.*;

/**
 * JAXB class for the request element: ObtenLogin
 * Matches <wsim:ObtenLogin> ... </wsim:ObtenLogin> in the WSDL.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObtenLogin", propOrder = {
        "userID",
        "strPwd",
        "proyectoID",
        "ip",
        "strOrigen",
        "rRespuesta"
})
@XmlRootElement(name = "ObtenLogin", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
public class ObtenLoginRequest {

    @XmlElement(name = "UserID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String userID;

    @XmlElement(name = "strPwd", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String strPwd;

    @XmlElement(name = "ProyectoID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer proyectoID; // Integer permite ausencia; ponte a 'int' si es siempre requerido

    @XmlElement(name = "IP", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String ip;

    @XmlElement(name = "strOrigen", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String strOrigen;

    /**
     * rRespuesta suele usar el namespace del servicio de usuarios.
     * Mantengo el namespace de WsUsuarios para que coincida con el XML mostrado.
     */
    @XmlElement(name = "rRespuesta", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Respuesta rRespuesta;

    public ObtenLoginRequest() {
    }

    // getters / setters

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getStrPwd() {
        return strPwd;
    }

    public void setStrPwd(String strPwd) {
        this.strPwd = strPwd;
    }

    public Integer getProyectoID() {
        return proyectoID;
    }

    public void setProyectoID(Integer proyectoID) {
        this.proyectoID = proyectoID;
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }

    public String getStrOrigen() {
        return strOrigen;
    }

    public void setStrOrigen(String strOrigen) {
        this.strOrigen = strOrigen;
    }

    public Respuesta getRRespuesta() {
        return rRespuesta;
    }

    public void setRRespuesta(Respuesta rRespuesta) {
        this.rRespuesta = rRespuesta;
    }
}
