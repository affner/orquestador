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
public class AccountRequestEntity {


    @JsonProperty("contrato")
    private String contractId;

    @JsonProperty("mes")
    private String month;

    @JsonProperty("anio")
    private String year;

    @JsonProperty("negocio")
    private String businessId;

    @JsonProperty("validez")
    private String validityId;

    @JsonProperty("credito")
    private String credit;
}
