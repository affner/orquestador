// mx/com/actinver/orquestador/dto/PaginatedComprobanteResponseDto.java
package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaginatedComprobanteResponseDto {
//
//    @ApiModelProperty(notes = "Lista de comprobantes encontrados en la página actual.", required = true)
//    private List<ComprobanteResultDto> content;

    @ApiModelProperty(notes = "Número de página actual (basado en 0).", example = "0", required = true)
    private int pageNumber;

    @ApiModelProperty(notes = "Tamaño de la página solicitada.", example = "10", required = true)
    private int pageSize;

    @ApiModelProperty(notes = "Número total de elementos que coinciden con los filtros (sin paginar).", example = "150", required = true)
    private long totalElements;

    @ApiModelProperty(notes = "Número total de páginas disponibles para los filtros aplicados.", example = "15", required = true)
    private int totalPages;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @ApiModelProperty(notes = "Mensaje de error, si la operación no fue exitosa o no se encontraron resultados con los filtros. Ausente en caso de éxito y resultados.", example = "No se encontraron comprobantes con los filtros proporcionados.", allowEmptyValue = false)
    private String error;

}