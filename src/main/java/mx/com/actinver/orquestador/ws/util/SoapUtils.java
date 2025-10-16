package mx.com.actinver.orquestador.ws.util;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;

/** Utilidades STaX para extraer el nombre de la operación SOAP. */
public final class SoapUtils {

    private SoapUtils() { /* utility-class */ }

    /**
     * Devuelve el <code>localPart</code> del primer elemento dentro de &lt;soap:Body&gt;
     *  – por ejemplo «ObtenLogin», «ContestaExpedientexLlave», …
     *  Si algo falla devuelve <code>null</code>.
     */
    public static String getOperationLocalPart(String xml) {
        XMLStreamReader r = null;
        try {
            r = XMLInputFactory.newInstance()
                    .createXMLStreamReader(new StringReader(xml));

            // Avanza hasta <soap:Body>
            while (r.hasNext()) {
                if (r.next() == XMLStreamConstants.START_ELEMENT &&
                        "Body".equals(r.getLocalName())) {

                    // El siguiente START_ELEMENT es la operación
                    while (r.hasNext()) {
                        if (r.next() == XMLStreamConstants.START_ELEMENT) {
                            return r.getLocalName();   // ← ObtenLogin, etc.
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // log.debug("No se pudo parsear SOAP", ignored);
        } finally {
            if (r != null) {               // cerrar manualmente
                try { r.close(); } catch (Exception e) { /* ignore */ }
            }
        }
        return null;
    }
}
