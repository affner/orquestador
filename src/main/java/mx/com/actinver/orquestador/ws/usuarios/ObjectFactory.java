package mx.com.actinver.orquestador.ws.usuarios;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;

/**
 * ObjectFactory para el paquete mx.com.actinver.orquestador.ws.usuarios
 * Provee JAXBElement wrappers para los elementos globales usados por JAXB.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _IDTicket_QNAME =
            new QName("http://Digipro.servicios/WsUsuarios/WsUsuarios", "IDTicket");
    private final static QName _Respuesta_QNAME =
            new QName("http://Digipro.servicios/WsUsuarios/WsUsuarios", "Respuesta");
    private final static QName _ObtenLoginResponse_QNAME =
            new QName("http://Digipro.servicios/WsUsuarios/WsUsuarios", "ObtenLoginResponse");

    public ObjectFactory() {
    }

    /** Factory methods (opcionalmente útiles para construir instancias programáticamente) */
    public IDTicket createIDTicket() {
        return new IDTicket();
    }

    public Respuesta createRespuesta() {
        return new Respuesta();
    }

    public ObtenLoginResponse createObtenLoginResponse() {
        return new ObtenLoginResponse();
    }

    /** JAXBElement wrappers con el namespace correcto (WsUsuarios) */
    @XmlElementDecl(namespace = "http://Digipro.servicios/WsUsuarios/WsUsuarios", name = "IDTicket")
    public JAXBElement<IDTicket> createIDTicket(IDTicket value) {
        return new JAXBElement<>(_IDTicket_QNAME, IDTicket.class, null, value);
    }

    @XmlElementDecl(namespace = "http://Digipro.servicios/WsUsuarios/WsUsuarios", name = "Respuesta")
    public JAXBElement<Respuesta> createRespuesta(Respuesta value) {
        return new JAXBElement<>(_Respuesta_QNAME, Respuesta.class, null, value);
    }

    @XmlElementDecl(namespace = "http://Digipro.servicios/WsUsuarios/WsUsuarios", name = "ObtenLoginResponse")
    public JAXBElement<ObtenLoginResponse> createObtenLoginResponse(ObtenLoginResponse value) {
        return new JAXBElement<>(_ObtenLoginResponse_QNAME, ObtenLoginResponse.class, null, value);
    }
}
