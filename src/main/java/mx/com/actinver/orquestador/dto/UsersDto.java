package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.entity.UsersEntity;
import mx.com.actinver.orquestador.util.MappingHelper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class UsersDto {

	private Long userId;

	private Long userType;

	private String name;

	private String user;

	private Value value;

	private Integer status;

	public static UsersDtoBuilder builder() {
		return new UsersDtoBuilder();
	}

	public static UsersDtoBuilder builder(UsersEntity data) {
		UsersDtoBuilder builder = builder();
		
		builder.userId(data.getUserId())
		.userType(data.getUserType())
		.name(data.getName())
		.user(data.getUser())
		.value(MappingHelper.toClass(data.getValue(), Value.class))
		.status(data.getStatus());
		
		return builder;
	}
	
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Value {

		private String employeeId;

		private String lastLoginIp;
		
		private String lastLoginDate;


	}
	
}
