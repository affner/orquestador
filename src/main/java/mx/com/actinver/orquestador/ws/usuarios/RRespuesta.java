package mx.com.actinver.orquestador.ws.usuarios;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import static mx.com.actinver.orquestador.constant.WsConstants.NS_WSUS;

/**
 * rRespuesta inner class: note fields are placed in the WsUsuarios namespace (default in final XML).
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RRespuesta", propOrder = { "codigo", "mensaje", "detalle" })
public class RRespuesta {

    @XmlElement(name = "Codigo", namespace = NS_WSUS)
    protected String codigo;

    @XmlElement(name = "Mensaje", namespace = NS_WSUS)
    protected String mensaje;

    @XmlElement(name = "Detalle", namespace = NS_WSUS)
    protected String detalle;


}