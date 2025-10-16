package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.util.ValueDeserializer;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProcessProgressesEntity {

    @JsonProperty("FIPRODID")
    private Long prodId;

    @JsonProperty("FIFLOWID")
    private Long stageId;

    @JsonProperty("FIPROGRESS")
    private Integer progress;

    @JsonProperty("FCMESSAGE") // si falla es el mensaje de error y al final este sera el total cargado tambien
    @JsonDeserialize(using = ValueDeserializer.class)
    private String message;

    @JsonProperty("FDTIMESTAMP")
    private String timestamp;

    public static ProcessProgressesEntityBuilder builder() {
        return new ProcessProgressesEntityBuilder();
    }


    
}
