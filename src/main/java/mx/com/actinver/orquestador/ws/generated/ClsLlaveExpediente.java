package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClsLlaveExpediente", propOrder = {"campos"})
public class ClsLlaveExpediente {

    @XmlElementWrapper(name="Campos")
    @XmlElement(name="ClsLlaveCampo")
    protected List<ClsLlaveCampo> campos;

    public List<ClsLlaveCampo> getCampos() {
        if (campos == null) campos = new ArrayList<>();
        return campos;
    }

    public void setCampos(List<ClsLlaveCampo> campos) { this.campos = campos; }
}
