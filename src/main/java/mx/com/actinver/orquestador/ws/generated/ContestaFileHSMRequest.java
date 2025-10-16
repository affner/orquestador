package mx.com.actinver.orquestador.ws.generated;

import mx.com.actinver.orquestador.ws.usuarios.IDTicket;

import javax.xml.bind.annotation.*;

/**
 * JAXB class for the request element: ContestaFileHSM
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContestaFileHSM", propOrder = {
        "docID",
        "proyID",
        "docIDPadreExp",
        "ticket"
})
@XmlRootElement(name = "ContestaFileHSM", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
public class ContestaFileHSMRequest {

    @XmlElement(name = "DocID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Long docID;

    @XmlElement(name = "ProyID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer proyID;

    @XmlElement(name = "DocIDPadreExp", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Long docIDPadreExp;

    @XmlElement(name = "Ticket", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected IDTicket ticket;

    public ContestaFileHSMRequest() {
    }

    public Long getDocID() {
        return docID;
    }

    public void setDocID(Long docID) {
        this.docID = docID;
    }

    public Integer getProyID() {
        return proyID;
    }

    public void setProyID(Integer proyID) {
        this.proyID = proyID;
    }

    public Long getDocIDPadreExp() {
        return docIDPadreExp;
    }

    public void setDocIDPadreExp(Long docIDPadreExp) {
        this.docIDPadreExp = docIDPadreExp;
    }

    public IDTicket getTicket() {
        return ticket;
    }

    public void setTicket(IDTicket ticket) {
        this.ticket = ticket;
    }
}
