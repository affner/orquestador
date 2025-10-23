package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class SpDateDeserializer extends StdDeserializer<ZonedDateTime> {

    public SpDateDeserializer() { super(ZonedDateTime.class); }
    @Override public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        String s = p.getValueAsString();
        if (s == null || s.isEmpty()) return null;
        // parsea como LocalDateTime en MX y lo promueve a Zoned
        LocalDateTime ldt = LocalDateTime.parse(s, SpDateFormat.SP_FMT);
        return ldt.atZone(SpDateFormat.MX_ZONE);
    }

}
