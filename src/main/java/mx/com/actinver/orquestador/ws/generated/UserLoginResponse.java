package mx.com.actinver.orquestador.ws.generated;

import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;

import javax.xml.bind.annotation.*;

/**
 * JAXB class for the response element: UserLoginResponse
 * Should contain the IDTicket (if login succeeded) and the Respuesta wrapper.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserLoginResponse", propOrder = {
        "idTicket",
        "rRespuesta"
})
@XmlRootElement(name = "UserLoginResponse", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
public class UserLoginResponse {

    @XmlElement(name = "IDTicket", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected IDTicket idTicket;

    @XmlElement(name = "rRespuesta", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Respuesta rRespuesta;

    public UserLoginResponse() {
    }

    // getters / setters

    public IDTicket getIDTicket() {
        return idTicket;
    }

    public void setIDTicket(IDTicket idTicket) {
        this.idTicket = idTicket;
    }

    public Respuesta getRRespuesta() {
        return rRespuesta;
    }

    public void setRRespuesta(Respuesta rRespuesta) {
        this.rRespuesta = rRespuesta;
    }
}
