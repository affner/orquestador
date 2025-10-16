package mx.com.actinver.orquestador.ws.generated;

import mx.com.actinver.orquestador.ws.usuarios.Respuesta;

import javax.xml.bind.annotation.*;

/**
 * JAXB class for the request element: UserLogin
 * Matches <UserLogin> ... </UserLogin> in the WSDL.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserLogin", propOrder = {
        "userID",
        "password",
        "strPwd",
        "ip",
        "strOrigen",
        "rRespuesta"
})
@XmlRootElement(name = "UserLogin", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
public class UserLogin {

    @XmlElement(name = "UserID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String userID;

    @XmlElement(name = "Password", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String password;

    @XmlElement(name = "strPwd", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String strPwd;

    @XmlElement(name = "IP", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String ip;

    @XmlElement(name = "strOrigen", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected String strOrigen;

    /**
     * Wrapper for response-info present in the request (optional in the screenshot).
     * We reuse your Respuesta class.
     */
    @XmlElement(name = "rRespuesta", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Respuesta rRespuesta;

    public UserLogin() {
    }

    // getters / setters

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getStrPwd() {
        return strPwd;
    }

    public void setStrPwd(String strPwd) {
        this.strPwd = strPwd;
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
