package mx.com.actinver.orquestador.ws.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

@XmlRegistry
public class ObjectFactory {
    private final static QName _ClsFileHSM_QNAME = new QName("http://Digipro.servicios/WsImagenes/WsImagenes", "clsFileHSM");
    private final static QName _ArrayOfClsFileHSM_QNAME = new QName("http://Digipro.servicios/WsImagenes/WsImagenes", "ArrayOfClsFileHSM");

    public ObjectFactory() {}

    public ClsFileHSM createClsFileHSM() { return new ClsFileHSM(); }
    public ArrayOfClsFileHSM createArrayOfClsFileHSM() { return new ArrayOfClsFileHSM(); }
    public ClsLlaveExpediente createClsLlaveExpediente() { return new ClsLlaveExpediente(); }
    public ClsLlaveCampo createClsLlaveCampo() { return new ClsLlaveCampo(); }

    @XmlElementDecl(namespace = "http://Digipro.servicios/WsImagenes/WsImagenes", name = "clsFileHSM")
    public JAXBElement<ClsFileHSM> createClsFileHSM(ClsFileHSM value) {
        return new JAXBElement<>(_ClsFileHSM_QNAME, ClsFileHSM.class, null, value);
    }
}
