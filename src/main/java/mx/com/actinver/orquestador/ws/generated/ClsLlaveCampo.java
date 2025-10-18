package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Mapea exactamente la estructura:
 * <wsim:clsLlaveCampo>
 *   <wsim:Campo>...</wsim:Campo>
 *   <wsim:Valor>...</wsim:Valor>
 * </wsim:clsLlaveCampo>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClsLlaveCampo", propOrder = {"campo", "valor", "tipoDato"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClsLlaveCampo {

    @XmlElement(name = "Campo")
    protected String campo;

    @XmlElement(name = "Valor")
    protected String valor;

    @XmlElement(name = "TipoDato")
    protected String tipoDato;
}

