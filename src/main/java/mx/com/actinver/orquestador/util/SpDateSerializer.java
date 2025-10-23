package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;

public class SpDateSerializer extends StdSerializer<ZonedDateTime> {
    public SpDateSerializer() { super(ZonedDateTime.class); }
    @Override public void serialize(ZonedDateTime value, JsonGenerator gen, SerializerProvider sp) throws IOException {
        if (value == null) { gen.writeNull(); return; }
        gen.writeString(SpDateFormat.SP_FMT.format(value));
    }
}
