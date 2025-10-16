package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class ProcessProgressDto {
	
	private Long prodId;
	private String processId;

	private Integer progress;
	private String message;

//	private List<CtrlNumberDto> result;
	
	public static ProcessProgressDtoBuilder builder() {
		return new ProcessProgressDtoBuilder();
	}
	
}
