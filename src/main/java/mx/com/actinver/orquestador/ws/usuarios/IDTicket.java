package mx.com.actinver.orquestador.ws.usuarios;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;

import static mx.com.actinver.orquestador.ws.WsConstants.NS_WSIM;
import static mx.com.actinver.orquestador.ws.WsConstants.NS_WSUS;

@Data @AllArgsConstructor @NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IDTicket",
        propOrder = { "ticketID","usrID","ip","proyectoID","fechaExpiracion"})
/*  ←–  Nota el namespace: WSIM (sin prefijo)  */
@XmlRootElement(name = "IDTicket", namespace = NS_WSIM)
public class IDTicket {
    @XmlElement(name="TicketID", namespace = NS_WSUS)
    protected String ticketID;
    @XmlElement(name="UsrID", namespace = NS_WSUS)
    protected String usrID;
    @XmlElement(name="IP", namespace = NS_WSUS)
    protected String ip;
    @XmlElement(name="ProyectoID", namespace = NS_WSUS)
    protected Integer proyectoID;
    @XmlElement(name="FechaExpiracion", namespace = NS_WSUS)
    protected XMLGregorianCalendar fechaExpiracion;

}
