package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UUIDRequestDto {

    @JsonProperty("idProduccion")
    private Long idProduccion;

    @JsonProperty("pagina")
    private Integer pagina;

    @JsonProperty("idNegocio")
    private Long businessId;

    public static UUIDRequestDtoBuilder builder() {
        return new UUIDRequestDtoBuilder();
    }
}
