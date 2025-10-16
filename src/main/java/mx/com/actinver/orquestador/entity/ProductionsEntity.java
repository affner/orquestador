package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.dto.ProductionsDto;
import mx.com.actinver.orquestador.util.ValueDeserializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class ProductionsEntity {

	@JsonProperty("FIPRODID")
	private Long prodId;

	@JsonProperty("FIYEAR")
	private Integer year;

	@JsonProperty("FIMONTH")
	private Integer month;

	@JsonProperty("FIENTITYID")
	private Long businessId;

	@JsonProperty("FITYPEID")
	private Long typeId;

	@JsonProperty("FIVERSIONID")
	private Long versionId;

	@JsonProperty("FIVALIDITY")
	private Long validityId;

	@JsonProperty("FIMODE")
	private Integer mode;

	@JsonProperty("FJRESUME")
	@JsonDeserialize(using = ValueDeserializer.class)
	private String resume;

	@JsonProperty("FIPRODSTAT")
	private Long statusId;

	@JsonProperty("FDCREATION")
	private String creationDate;
	
	@JsonProperty("FICREATEDBY")
	private Long createdBy;

	@JsonProperty("FDLASTUPDATE")
	private String lastUpdateDate;

	@JsonProperty("FIUPDATEDBY")
	private Long updateBy;

	public static ProductionsEntityBuilder builder() {
		return new ProductionsEntityBuilder();
	}

	public static ProductionsEntityBuilder builder(ProductionsDto data) {
		ProductionsEntityBuilder builder = builder();
		
		builder.prodId(data.getProdId())
		.year(data.getYear())
		.month(data.getMonth())
		.businessId(data.getBusinessId())
		.typeId(data.getTypeId())
		.versionId(data.getVersionId())
		.validityId(data.getValidityId())
		.mode(data.getMode())
//		.resume(MappingHelper.toJson(data.getResume()))
		.statusId(data.getStatusId())
		.creationDate(data.getCreationDate())
		.createdBy(data.getCreatedBy())
		.lastUpdateDate(data.getLastUpdateDate())
		.updateBy(data.getUpdateBy());
		
		return builder;
	}
	
}
