package mx.com.actinver.orquestador.ws.usuarios;

import lombok.*;

import javax.xml.bind.annotation.*;

import static mx.com.actinver.orquestador.ws.WsConstants.NS_WSIM;
import static mx.com.actinver.orquestador.ws.WsConstants.NS_WSUS;

/**
 * Representa: <ObtenLoginResponse xmlns:ns2="http://Digipro.servicios/WsImagenes/WsImagenes" xmlns="http://Digipro.servicios/WsUsuarios/WsUsuarios">
 *                <ns2:rRespuesta>
 *                   <Codigo>...</Codigo>
 *                   <Mensaje>...</Mensaje>
 *                   <Detalle>...</Detalle>
 *                </ns2:rRespuesta>
 *                <IDTicket>... (en WSUsuarios namespace por defecto)</IDTicket>
 *              </ObtenLoginResponse>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ObtenLoginResponse", propOrder = {
        "rRespuesta",
        "idTicket"
})
@XmlRootElement(name = "ObtenLoginResponse", namespace = NS_WSIM)
public class ObtenLoginResponse {


    /** rRespuesta element is placed in the WsImagenes namespace (will appear as ns2:rRespuesta) */
    @XmlElement(name = "rRespuesta", namespace = NS_WSIM, required = false)
    protected RRespuesta rRespuesta;

    /** IDTicket will be marshalled in the WsUsuarios namespace (we want this to be default in output). */

    @XmlElement(name = "IDTicket", namespace = NS_WSUS, required = false)
    protected IDTicket idTicket;





}
