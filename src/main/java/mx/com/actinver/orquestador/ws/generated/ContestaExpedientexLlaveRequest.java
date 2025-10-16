package mx.com.actinver.orquestador.ws.generated;

import mx.com.actinver.orquestador.ws.usuarios.IDTicket;

import javax.xml.bind.annotation.*;

/**
 * JAXB class for the request element: ContestaExpedientexLlave
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContestaExpedientexLlave", propOrder = {
        "ticket",
        "llave",
        "proyID",
        "expedienteID",
        "tipoDocID"
})
@XmlRootElement(name = "ContestaExpedientexLlave", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
public class ContestaExpedientexLlaveRequest {

    @XmlElement(name = "Ticket", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected IDTicket ticket;

    @XmlElement(name = "llave", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected ClsLlaveExpediente llave;

    /**
     * Usamos Integer en lugar de short para permitir ausencia del elemento.
     */
    @XmlElement(name = "ProyID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer proyID;

    @XmlElement(name = "ExpedienteID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer expedienteID;

    @XmlElement(name = "TipoDocID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer tipoDocID;

    public ContestaExpedientexLlaveRequest() {
    }

    public IDTicket getTicket() {
        return ticket;
    }

    public void setTicket(IDTicket ticket) {
        this.ticket = ticket;
    }

    public ClsLlaveExpediente getLlave() {
        return llave;
    }

    public void setLlave(ClsLlaveExpediente llave) {
        this.llave = llave;
    }

    public Integer getProyID() {
        return proyID;
    }

    public void setProyID(Integer proyID) {
        this.proyID = proyID;
    }

    public Integer getExpedienteID() {
        return expedienteID;
    }

    public void setExpedienteID(Integer expedienteID) {
        this.expedienteID = expedienteID;
    }

    public Integer getTipoDocID() {
        return tipoDocID;
    }

    public void setTipoDocID(Integer tipoDocID) {
        this.tipoDocID = tipoDocID;
    }
}
