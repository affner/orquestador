package mx.com.actinver.orquestador.util;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SpDateFormat {

    public static final ZoneId MX_ZONE = ZoneId.of("America/Mexico_City");

    public static final DateTimeFormatter SP_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", new Locale("es", "MX"))
                    .withZone(MX_ZONE);

    private SpDateFormat() {
    }

}
