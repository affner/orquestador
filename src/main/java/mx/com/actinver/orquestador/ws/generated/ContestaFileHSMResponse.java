package mx.com.actinver.orquestador.ws.generated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;
import mx.com.actinver.orquestador.ws.usuarios.Respuesta;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * JAXB representation for the ContestaFileHSM response element.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContestaFileHSMResponse", propOrder = {
        "contestaFileHSMResult",
        "ticket",
        "rRespuesta"
})
@XmlRootElement(name = "ContestaFileHSMResponse", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestaFileHSMResponse {

    @XmlElement(name = "ContestaFileHSMResult", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected ClsFileHSM contestaFileHSMResult;

    @XmlElement(name = "Ticket", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected IDTicket ticket;

    @XmlElement(name = "rRespuesta", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Respuesta rRespuesta;


}