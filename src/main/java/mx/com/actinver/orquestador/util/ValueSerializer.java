package mx.com.actinver.orquestador.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class ValueSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && value.trim().startsWith("{")) {
            // Parsea el string como objeto JSON
            gen.writeRawValue(value);
        } else {
            // Fallback a string normal
            gen.writeString(value);
        }
    }
}
