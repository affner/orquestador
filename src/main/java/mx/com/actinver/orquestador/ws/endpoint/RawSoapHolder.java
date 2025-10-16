package mx.com.actinver.orquestador.ws.endpoint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Holder simple por request (ThreadLocal) para mantener el SOAP crudo mientras se procesa la petición.
 */
public final class RawSoapHolder {
    private static final Logger LOG = LogManager.getLogger(RawSoapHolder.class);
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private RawSoapHolder() {}

    public static void set(String xml) {
        HOLDER.set(xml);
        LOG.debug("[RawSoapHolder] set raw SOAP (len={})", xml != null ? xml.length() : 0);
    }

    public static String get() {
        return HOLDER.get();
    }

    public static void clear() {
        String old = HOLDER.get();
        if (old != null) {
            LOG.debug("[RawSoapHolder] clearing raw SOAP (len={})", old.length());
        }
        HOLDER.remove();
    }
}
