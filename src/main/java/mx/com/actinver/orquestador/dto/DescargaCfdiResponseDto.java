package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa la respuesta del servicio REST de descarga de CFDI.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DescargaCfdiResponseDto {

    private String fileName;
    private byte[] fileData;
    private String fileType;
    private String mimeType;
    private String message;
}