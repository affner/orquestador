package mx.com.actinver.auth.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequestDto {

	@ApiModelProperty(required = true, notes = "Identificador del cliente.")
	private String username;

	@ApiModelProperty(required = true, notes = "Clave secreta del cliente.")
	private String password;

}
