package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.dto.TemplateContent;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class AccStmtFinderEntity {

	@JsonProperty("FICLIENTE")
	private Long client;

	@JsonProperty("FIYEAR")
	private Integer year;

	@JsonProperty("FIMONTH")
	private Integer month;

	@JsonProperty("FICONTRATO")
	private Long contract;

	@JsonProperty("FICREDITO")
	private Long credit;

	@JsonProperty("FJVERSION")
	private TemplateContent version;

//	@JsonProperty("FJXSAINFO")
//	@JsonDeserialize(using = ValueDeserializer.class)
//	private String xsa;

	public static AccStmtFinderEntityBuilder builder() {
		return new AccStmtFinderEntityBuilder();
	}

}
