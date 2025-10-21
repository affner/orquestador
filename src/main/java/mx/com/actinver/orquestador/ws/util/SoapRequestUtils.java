package mx.com.actinver.orquestador.ws.util;


import mx.com.actinver.conf.DynamicString;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


import javax.xml.bind.*;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SoapRequestUtils {

    private static final org.apache.logging.log4j.Logger LOG = org.apache.logging.log4j.LogManager.getLogger(SoapRequestUtils.class);

    private static volatile JAXBContext PACKAGE_CTX;
    private static final String CONTEXT_PATH =
            "mx.com.actinver.orquestador.ws.generated:mx.com.actinver.orquestador.ws.usuarios";


    // Cache de la clase de request por operación (ContestaExpedientexLlave -> Class)
    private static final Map<String, Class<?>> REQ_CLASS_BY_OP = new ConcurrentHashMap<>();




    /** Crea/cacha un JAXBContext con las clases relevantes (puedes centralizarlo fuera si prefieres) */
    public static JAXBContext buildContext(Class<?>... types) {
        try {
            return JAXBContext.newInstance(types);
        } catch (JAXBException e) {
            throw new RuntimeException("No se pudo crear JAXBContext", e);
        }
    }


    public <T> String genericMarshall(T data) throws JAXBException {

        JAXBContext jaxbContext = JAXBContext.newInstance(data.getClass());
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        StringWriter stringWriter = new StringWriter();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(data, stringWriter);
        return stringWriter.toString();

    }

    // Quita BOM y espacios molestos
    private String sanitize(String xml) {
        if (xml == null) return "";
        String s = xml;
        if (!s.isEmpty() && s.charAt(0) == '\uFEFF') s = s.substring(1);
        return s.trim();
    }

    private Document parseNsAware(String xml) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static Element getFirstBodyChild(Document doc) {
        String[] soapNS = {
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.w3.org/2003/05/soap-envelope"
        };
        Element body = null;
        for (String ns : soapNS) {
            NodeList nl = doc.getElementsByTagNameNS(ns, "Body");
            if (nl != null && nl.getLength() > 0) { body = (Element) nl.item(0); break; }
        }
        if (body == null) {
            NodeList nl = doc.getElementsByTagName("Body");
            if (nl != null && nl.getLength() > 0) body = (Element) nl.item(0);
        }
        if (body == null) return null;

        // primer hijo ELEMENT (omite textos/espacios)
        org.w3c.dom.Node n = body.getFirstChild();
        while (n != null && n.getNodeType() != Node.ELEMENT_NODE) n = n.getNextSibling();
        return (Element) n;
    }
    public <T> T unmarshalFromSoapBody(String rawXml, Class<T> clazz) throws Exception {
        Document doc = parseNsAware(sanitize(rawXml));
        Element payload = getFirstBodyChild(doc);
        if (payload == null) return null;

        JAXBContext ctx = getPackageContext(); // tu contexto por paquetes (con TCCL)
        try {
            Unmarshaller um = ctx.createUnmarshaller();
            JAXBElement<T> j = um.unmarshal(new DOMSource(payload), clazz);
            return j.getValue();
        } catch (JAXBException ex) {
            // Fallback 1: contexto per-class (no requiere ClassLoader explícito)
            try {
                JAXBContext perClass = JAXBContext.newInstance(clazz);
                Unmarshaller um2 = perClass.createUnmarshaller();
                JAXBElement<T> j2 = um2.unmarshal(new DOMSource(payload), clazz);
                return j2.getValue();
            } catch (JAXBException ex2) {
                // Fallback 2: contexto por package + TCCL (requiere ObjectFactory en el paquete)
                try {
                    String pkg = clazz.getPackage().getName();
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    JAXBContext byPkg = JAXBContext.newInstance(pkg, cl);
                    Unmarshaller um3 = byPkg.createUnmarshaller();
                    Object o = um3.unmarshal(new DOMSource(payload));
                    if (o instanceof JAXBElement) o = ((JAXBElement<?>) o).getValue();
                    return clazz.cast(o);
                } catch (JAXBException | ClassCastException ex3) {
                    // Si tampoco, re-lanza el original (más representativo del fallo real)
                    throw ex;
                }
            }
        }
    }




    public <T> T unmarshalFromSoapBody(String rawXml, Class<T> clazz, JAXBContext ctx) throws Exception {
        String xml = sanitize(rawXml);
        Document doc = parseNsAware(xml);
        Element payload = getFirstBodyChild(doc);
        if (payload == null) return null;

        String ln = payload.getLocalName();
        String ns = payload.getNamespaceURI();
        LOG.info("[SOAP DEBUG] payload localName= {}  ns= {}", ln, ns);

        // fallback seguro si ctx es null
        if (ctx == null) {
            try {
                ctx = JAXBContext.newInstance(clazz);  // contexto mínimo solo con la clase solicitada
            } catch (JAXBException e) {
                throw new RuntimeException("No se pudo crear JAXBContext dinámico para " + clazz.getName(), e);
            }
        }

        Unmarshaller um = ctx.createUnmarshaller();

        try {
            @SuppressWarnings("unchecked")
            JAXBElement<T> j = um.unmarshal(new DOMSource(payload), clazz);
            return j.getValue();
        } catch (UnmarshalException ignoreIfNoRoot) {
            Object obj = um.unmarshal(new DOMSource(payload));
            if (clazz.isInstance(obj)) return clazz.cast(obj);
            if (obj instanceof JAXBElement) {
                Object v = ((JAXBElement<?>) obj).getValue();
                if (clazz.isInstance(v)) return clazz.cast(v);
            }
            return null;
        }
    }


    public String base64ToStringConversion(String bytes) {
        return new String(Base64.decodeBase64(bytes));
    }

    public String stringToBase64Conversion(String text) {
        Base64 base64 = new Base64();
        return new String(base64.encode(text.getBytes()));

    }

    public String getTodayDatexml() {
        try {

            XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
            return date.toString();

        } catch (DatatypeConfigurationException e) {
            e.printStackTrace();
        }

        return null;
    }

    public XMLGregorianCalendar getDatexml() throws DatatypeConfigurationException {


        XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        return date;

    }



    public SOAPMessage getSoapMessageFromXml(String xml) throws SOAPException, IOException {
        MessageFactory factory = MessageFactory.newInstance();
        MimeHeaders header = new MimeHeaders();
        header.setHeader("soapAction", "operation1");
        SOAPMessage soapMessage = factory.createMessage(header, new ByteArrayInputStream(xml.getBytes(Charset.forName("UTF-8"))));
        return soapMessage;

    }

    public SOAPMessage sendSoapMessage(SOAPMessage request, String url) throws SOAPException {

        SOAPConnectionFactory soapConnectionFactory = null;

        soapConnectionFactory = SOAPConnectionFactory.newInstance();

        SOAPConnection soapConnection = soapConnectionFactory.createConnection();

        SOAPMessage soapResponse = soapConnection.call(request, url);

        return soapResponse;

    }

    public String getValueFromResponseBody(SOAPMessage soapMessage, String tagName) {
        try {
            return soapMessage.getSOAPPart().getEnvelope()
                    .getBody().getElementsByTagName(tagName).item(0)
                    .getTextContent();
        } catch (Exception e) {
            return null;
        }

    }

    public Document setValueFromXml( Document doc , String name, String value) {

        doc.getDocumentElement().normalize();
        org.w3c.dom.Node nodVal = doc.getElementsByTagName(name).item(0);
        nodVal.setTextContent(value);

        return doc;
    }

    public SOAPMessage setSoapMessageFromXml(SOAPMessage request, Document xml) {

        SOAPBody soapBody = null;
        try {
            soapBody = request.getSOAPBody();
            soapBody.removeContents();
            soapBody.addDocument(xml);
            return request;
        } catch (SOAPException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getValueFromXml(Document doc, String tagName) {

        doc.getDocumentElement().normalize();
        org.w3c.dom.Node nodVal = doc.getElementsByTagName(tagName).item(0);
        String value = nodVal.getFirstChild().getTextContent();
        return value;
    }

    public String nodeToString(org.w3c.dom.Node node) throws Exception {
        StringWriter sw = new StringWriter();

        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));

        return sw.toString();
    }

    public String convertToString(SOAPMessage message) throws IOException, SOAPException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        message.writeTo(out);

        String strMsg = new String(out.toByteArray());
        return strMsg;
    }

    public String streamSourceToString(StreamSource ss) throws IOException {
        if (ss == null) return null;

        // si fue construido con Reader (ej. new StreamSource(new StringReader(...)))
        Reader r = ss.getReader();
        if (r != null) {
            try (Reader reader = r) {
                StringBuilder sb = new StringBuilder();
                char[] buf = new char[4096];
                int n;
                while ((n = reader.read(buf)) != -1) sb.append(buf, 0, n);
                return sb.toString();
            }
        }

        // si fue construido con InputStream
        InputStream is = ss.getInputStream();
        if (is != null) {
            try (InputStream in = is; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = in.read(buf)) != -1) bos.write(buf, 0, n);
                return bos.toString(StandardCharsets.UTF_8.name());
            }
        }

        // fallback: no hay reader ni inputstream -> intentar fuente de sistema (no usual)
        throw new IllegalArgumentException("StreamSource no contiene Reader ni InputStream");
    }


    public ChronoLocalDate parseCutoverDateOrDefault(DynamicString migrationCutoverDate) {
        if (migrationCutoverDate == null) {
            LOG.warn("migrationCutoverDate es null, usando comportamiento por defecto (legacy).");
            return LocalDate.MAX; // fuerza legacy por seguridad
        }

        String raw = migrationCutoverDate.toString();
        if (raw == null || raw.isEmpty()) {
            LOG.warn("migrationCutoverDate vacío, usando comportamiento por defecto (legacy).");
            return LocalDate.MAX;
        }

        // normalizar: quitar espacios, comillas, tomar antes de 'T' si viene datetime
        raw = raw.trim().replace("\"", "");
        int tIdx = raw.indexOf('T');
        if (tIdx > -1) raw = raw.substring(0, tIdx);
        // si viene con hora separada por espacio, tomar antes del espacio
        int sp = raw.indexOf(' ');
        if (sp > -1) raw = raw.substring(0, sp);
        raw = raw.trim();

        // formatos probados (ordenado por mayor probabilidad)
        DateTimeFormatter[] fmts = new DateTimeFormatter[]{
                DateTimeFormatter.ISO_LOCAL_DATE,            // 2025-10-20
                DateTimeFormatter.ofPattern("yyyyMMdd"),    // 20251020
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),  // 20/10/2025
                DateTimeFormatter.ofPattern("d/M/yyyy"),    // 2/5/2025
                DateTimeFormatter.ofPattern("dd-MM-yyyy")   // 20-10-2025
        };

        for (DateTimeFormatter fmt : fmts) {
            try {
                LocalDate ld = LocalDate.parse(raw, fmt);
                LOG.debug("Cutover date parsed '{}' -> {}", raw, ld);
                return ld;
            } catch (DateTimeParseException e) {
                // intentar siguiente formato
            }
        }

        // si llegó aquí, no parseó: warn y fallback seguro a LocalDate.MAX (forzar legacy)
        LOG.warn("No se pudo parsear migrationCutoverDate='{}'. Asumir comportamiento legacy (fecha de corte no válida).", raw);
        return LocalDate.MAX;
    }



    /** Resuelve la clase del request en runtime a partir del nombre de operación. */
    public static Class<?> resolveRequestClass(String operationLocalPart) {
        return REQ_CLASS_BY_OP.computeIfAbsent(operationLocalPart, op -> {
            String base = "mx.com.actinver.orquestador.ws.generated.";
            String[] candidates = new String[] {
                    base + op + "Request",  // p.ej. ContestaExpedientexLlaveRequest
                    base + op               // por si tus clases NO usan sufijo Request
            };
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (String fqcn : candidates) {
                try { return Class.forName(fqcn, true, cl); }
                catch (ClassNotFoundException ignore) {}
            }
            return null;
        });
    }

    public static JAXBContext getPackageContext() {
        if (PACKAGE_CTX == null) {
            synchronized (SoapRequestUtils.class) {
                if (PACKAGE_CTX == null) {
                    ClassLoader cl = Thread.currentThread().getContextClassLoader();
                    try {
                        PACKAGE_CTX = JAXBContext.newInstance(CONTEXT_PATH, cl);
                    } catch (JAXBException e) {
                        throw new RuntimeException("No se pudo crear JAXBContext para: " + CONTEXT_PATH, e);
                    }
                }
            }
        }
        return PACKAGE_CTX;
    }

}
