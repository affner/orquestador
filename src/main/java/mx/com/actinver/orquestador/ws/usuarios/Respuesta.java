package mx.com.actinver.orquestador.ws.usuarios;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name="Respuesta", propOrder = {"respuestaID","categoria","descripcionRespuesta"})
@XmlRootElement(name="Respuesta")
public class Respuesta {
    @XmlElement(name="RespuestaID")
    protected String respuestaID;
    @XmlElement(name="Categoria")
    protected String categoria;
    @XmlElement(name="DescripcionRespuesta")
    protected String descripcionRespuesta;

    public String getRespuestaID() { return respuestaID; }
    public void setRespuestaID(String v) { this.respuestaID = v; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String v) { this.categoria = v; }

    public String getDescripcionRespuesta() { return descripcionRespuesta; }
    public void setDescripcionRespuesta(String v) { this.descripcionRespuesta = v; }
}
