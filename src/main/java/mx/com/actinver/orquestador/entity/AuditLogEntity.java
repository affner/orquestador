package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.util.SpDateDeserializer;
import mx.com.actinver.orquestador.util.SpDateSerializer;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditLogEntity {

    @JsonProperty("FDSTART")
    @JsonSerialize(using = SpDateSerializer.class)
    @JsonDeserialize(using = SpDateDeserializer.class)
    private ZonedDateTime startAt;

    @JsonProperty("FCPACKAGE")
    private String paq  ;

    @JsonProperty("FCPROCEDURE")
    private String proc;

    @JsonProperty("FCRVALUES")
    @JsonRawValue
    private String value;

    @JsonProperty("FCRESULT")
    @JsonRawValue
    private String result;

    public static AuditLogEntityBuilder builder() {
        return new AuditLogEntityBuilder();
    }
}
