package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class AccStmtRequestDto {

	@JsonProperty("negocio")
	@JsonAlias({ "business" })
	private Long business;

	@JsonProperty("anio")
	@JsonAlias({ "year" })
	private Long year;

	@JsonProperty("mes")
	@JsonAlias({ "month" })
	private Long month;

	@JsonProperty("contrato")
	@JsonAlias({ "contract" })
	private Long contract;

	@JsonProperty("credito")
	@JsonAlias({ "credit" })
	private Long credit;

	@ApiModelProperty(hidden = true)
	@JsonProperty(value = "validez", access = Access.READ_ONLY)
	private Long validity;

}
