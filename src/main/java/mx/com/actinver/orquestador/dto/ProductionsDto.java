package mx.com.actinver.orquestador.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.entity.ProductionsEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class ProductionsDto {

	private Long prodId;

	private Integer year;

	private Integer month;

	private Long businessId;

	private Long typeId;

	private Long versionId;

	private Long validityId;

	private Integer mode;

//	private ProductionResultsDto resume;

	private Long statusId;

	private String creationDate;

	private Long createdBy;

	private String lastUpdateDate;

	private Long updateBy;

	//@JsonProperty("FDSTART")
	private String startDate;

	//@JsonProperty("FDEND")
	private String endDate;

	//@JsonProperty("FIPAGE")
	private Integer page;

	public static ProductionsDtoBuilder builder() {
		return new ProductionsDtoBuilder();
	}

	public static ProductionsDtoBuilder builder(ProductionsEntity data) {
		ProductionsDtoBuilder builder = builder();

		builder.prodId(data.getProdId())
				.year(data.getYear())
				.month(data.getMonth())
				.businessId(data.getBusinessId())
				.typeId(data.getTypeId())
				.versionId(data.getVersionId())
				.validityId(data.getValidityId())
				.mode(data.getMode())
//				.resume(MappingHelper.toClass(data.getResume(), ProductionResultsDto.class))
				.statusId(data.getStatusId())
				.creationDate(data.getCreationDate())
				.createdBy(data.getCreatedBy())
				.lastUpdateDate(data.getLastUpdateDate())
				.updateBy(data.getUpdateBy());

		return builder;
	}

}

