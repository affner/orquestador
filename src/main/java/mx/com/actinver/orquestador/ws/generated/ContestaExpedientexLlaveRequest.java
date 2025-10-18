package mx.com.actinver.orquestador.ws.generated;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.ws.usuarios.IDTicket;

import javax.xml.bind.*;
import javax.xml.bind.annotation.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.io.StringReader;

/**
 * JAXB class for the request element: ContestaExpedientexLlave
 * - Usamos @XmlAccessorType(XmlAccessType.FIELD) para JAXB sobre campos.
 * - Lombok genera getters/setters y constructores.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ContestaExpedientexLlave", propOrder = {
        "ticket",
        "llave",
        "proyID",
        "expedienteID",
        "tipoDocID"
})
@XmlRootElement(name = "ContestaExpedientexLlave", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContestaExpedientexLlaveRequest {

    @XmlElement(name = "Ticket", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected IDTicket ticket;

    @XmlElement(name = "llave", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected ClsLlaveExpediente llave;

    /** Usamos Integer en lugar de short para permitir ausencia del elemento. */
    @XmlElement(name = "ProyID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer proyID;

    @XmlElement(name = "ExpedienteID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer expedienteID;

    @XmlElement(name = "TipoDocID", namespace = "http://Digipro.servicios/WsImagenes/WsImagenes")
    protected Integer tipoDocID;

    /* ----------------- helper estático para construir desde XML (SOAP o fragmento) ----------------- */

    /**
     * Sobrecarga "builder" para que puedas usar: ContestaExpedientexLlaveRequest.builder(rawSoap).build();
     * NOTA: Lombok también generará builder() sin parámetros; esta es una sobrecarga estática distinta.
     */
    public static XmlStringBuilder builder(String rawXml) {
        return new XmlStringBuilder(rawXml);
    }

    public static class XmlStringBuilder {
        private final String rawXml;

        public XmlStringBuilder(String rawXml) {
            this.rawXml = rawXml;
        }

        /**
         * Hace el parse y devuelve la instancia JAXB del request.
         * Lanza Exception en caso de error (puedes ajustar a JAXBException si prefieres).
         */
        public ContestaExpedientexLlaveRequest build() throws Exception {
            return fromXml(rawXml);
        }
    }

    /**
     * Unmarshal robusto que:
     *  - acepta raw SOAP (envelope) o solo el fragmento del request,
     *  - busca el elemento con namespace/name correcto y lo unmarshallea.
     */
    public static ContestaExpedientexLlaveRequest fromXml(String xml) throws Exception {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML vacío");
        }

        // parse DOM de forma namespace-aware
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        // intentar localizar el elemento por namespace y localName
        String targetNS = "http://Digipro.servicios/WsImagenes/WsImagenes";
        String localName = "ContestaExpedientexLlave";

        Node targetNode = null;
        NodeList nlist = doc.getElementsByTagNameNS(targetNS, localName);
        if (nlist != null && nlist.getLength() > 0) {
            targetNode = nlist.item(0);
        } else {
            // fallback: buscar por localName sin namespace (por si hay variaciones)
            NodeList all = doc.getElementsByTagName(localName);
            if (all != null && all.getLength() > 0) targetNode = all.item(0);
        }

        if (targetNode == null) {
            // última alternativa: si el documento raíz tiene el elemento directamente
            Element docEl = doc.getDocumentElement();
            if (docEl != null && (localName.equals(docEl.getLocalName()) || localName.equals(docEl.getNodeName()))) {
                targetNode = docEl;
            }
        }

        if (targetNode == null) {
            throw new JAXBException("No se encontró el elemento '" + localName + "' en el XML suministrado.");
        }

        // unmarshall desde el nodo DOM encontrado
        JAXBContext jc = JAXBContext.newInstance(ContestaExpedientexLlaveRequest.class);
        Unmarshaller um = jc.createUnmarshaller();
        Object obj = um.unmarshal(new DOMSource(targetNode));
        return (ContestaExpedientexLlaveRequest) obj;
    }

    /* Opcional: método genérico similar (recomendado mover a XmlUtils si lo usarás para otras clases) */
    public static <T> T fromXml(String xml, Class<T> clazz) throws Exception {
        if (xml == null || clazz == null) throw new IllegalArgumentException();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));

        // buscar por root localName del tipo T (si tiene @XmlRootElement usa ese nombre)
        XmlRootElement rootAnnotation = clazz.getAnnotation(XmlRootElement.class);
        String expectedLocalName = rootAnnotation != null ? rootAnnotation.name() : clazz.getSimpleName();

        Node targetNode = null;
        NodeList nlist = doc.getElementsByTagNameNS("*", expectedLocalName);
        if (nlist != null && nlist.getLength() > 0) targetNode = nlist.item(0);
        if (targetNode == null) {
            NodeList all = doc.getElementsByTagName(expectedLocalName);
            if (all != null && all.getLength() > 0) targetNode = all.item(0);
        }
        if (targetNode == null) throw new JAXBException("No se encontró elemento " + expectedLocalName);

        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller um = jc.createUnmarshaller();
        Object obj = um.unmarshal(new DOMSource(targetNode));
        return clazz.cast(obj);
    }
}
