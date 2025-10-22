package mx.com.actinver.orquestador.dto;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentResponseDto {
	private String processId; 
    private String fileName;
    private byte[] fileContent;
}

