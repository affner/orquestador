package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ArrayOfClsFileHSM", propOrder = { "clsFileHSM" })
public class ArrayOfClsFileHSM {

    @XmlElement(name = "clsFileHSM", nillable = true)
    protected List<ClsFileHSM> clsFileHSM;

    public List<ClsFileHSM> getClsFileHSM() {
        if (clsFileHSM == null) clsFileHSM = new ArrayList<>();
        return this.clsFileHSM;
    }
}
