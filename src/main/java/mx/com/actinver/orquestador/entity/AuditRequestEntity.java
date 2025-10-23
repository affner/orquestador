package mx.com.actinver.orquestador.entity;

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
public class AuditRequestEntity {


    @JsonProperty("contrato")
    private Long contractId;

    @JsonProperty("mes")
    private Integer month;

    @JsonProperty("anio")
    private Integer year;

    @JsonProperty("negocio")
    private Long businessId;

    @JsonProperty("validez")
    private Long validityId;

    @JsonProperty("credito")
    private Long credit;
}
