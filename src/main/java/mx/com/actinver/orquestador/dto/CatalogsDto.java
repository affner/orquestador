package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.entity.CatalogsEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class CatalogsDto {

	private Long catalogId;

	private Long parentId;

	private String name;

	private String description;

	private String value;

	private String value2;

	private Integer status;

	public static CatalogsDtoBuilder builder() {
		return new CatalogsDtoBuilder();
	}
	
	public static CatalogsDtoBuilder builder(CatalogsEntity data) {
		CatalogsDtoBuilder builder = builder();
		
		builder.catalogId(data.getCatalogId())
		.parentId(data.getParentId())
		.name(data.getName())
		.description(data.getDescription())
		.value(data.getValue())
				.value2(data.getValue2())
		.status(data.getStatus());
		
		return builder;
	}
	
}