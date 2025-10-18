package mx.com.actinver.orquestador.ws.generated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClsLlaveExpediente", propOrder = {"campos"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClsLlaveExpediente {

    /**
     * Mapea:
     * <Campos>
     *   <clsLlaveCampo>...</clsLlaveCampo>
     * </Campos>
     */
    @XmlElementWrapper(name = "Campos")
    @XmlElement(name = "clsLlaveCampo")
    protected List<ClsLlaveCampo> campos;

    public List<ClsLlaveCampo> getCampos() {
        if (campos == null) campos = new ArrayList<>();
        return campos;
    }

    public void setCampos(List<ClsLlaveCampo> campos) { this.campos = campos; }
}
