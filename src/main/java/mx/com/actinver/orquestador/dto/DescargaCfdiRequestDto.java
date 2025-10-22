package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

/**
 * DTO utilizado para invocar el servicio REST de descarga de CFDI.
 * Replica la definición utilizada en el microservicio de timbrado
 * para mantener compatibilidad con los parámetros esperados.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescargaCfdiRequestDto {

    /** Identificador del contrato. */

    private String contractId;

    /** Año del CFDI a consultar. */
    @NotNull
    private String year;

    /** Mes del CFDI a consultar. */
    @NotNull
    private String month;

    /** Identificador del negocio. */
    @NotNull
    private String businessId;

    private String validityId;

    /** Tipo de archivo requerido (PDF, XML o ZIP). */
    private String fileType;

    private String credit;
}