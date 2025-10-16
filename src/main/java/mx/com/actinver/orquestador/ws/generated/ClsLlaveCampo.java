package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClsLlaveCampo", propOrder = {"nombre","valor"})
public class ClsLlaveCampo {
    @XmlElement(name="Nombre")
    protected String nombre;
    @XmlElement(name="Valor")
    protected String valor;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
}
