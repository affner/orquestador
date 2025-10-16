package mx.com.actinver.orquestador.ws.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class WsImagenesPrefixMapper extends NamespacePrefixMapper {

    private static final String NS_WSIM = "http://Digipro.servicios/WsImagenes/WsImagenes";
    private static final String NS_WSUS = "http://Digipro.servicios/WsUsuarios/WsUsuarios";

    @Override
    public String getPreferredPrefix(String namespaceUri,
                                     String suggestion,
                                     boolean requirePrefix) {

        if (NS_WSIM.equals(namespaceUri)) {
            // <-- SIN prefijo: será el default xmlns="..."
            return "";
        }
        if (NS_WSUS.equals(namespaceUri)) {
            // prefijo constante para usuarios
            return "ns2";
        }
        // lo que JAXB prefiera para los demás
        return suggestion;
    }
}
