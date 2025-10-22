package mx.com.actinver.orquestador.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import mx.com.actinver.orquestador.dto.UsersDto;
import mx.com.actinver.orquestador.util.MappingHelper;
import mx.com.actinver.orquestador.util.ValueDeserializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class UsersEntity {

	@JsonProperty("FIUSERID")
	private Long userId;

	@JsonProperty("FIUSERTYPE")
	private Long userType;

	@JsonProperty("FCNAME")
	private String name;

	@JsonProperty("FCUSER")
	private String user;

	@JsonProperty("FCJVALUE")
	@JsonDeserialize(using = ValueDeserializer.class)
	private String value;

	@JsonProperty("FISTATUS")
	private Integer status;

	@JsonProperty("FIUPDATEDBY")
	private Long updateBy;

	@JsonProperty("FDCREATION")
	private String creationDate;

	@JsonProperty("FDLASTUPDATE")
	private String lastUpdateDate;

	public static UsersEntityBuilder builder() {
		return new UsersEntityBuilder();
	}

	public static UsersEntityBuilder builder(UsersDto data) {
		UsersEntityBuilder builder = builder();
		
		builder.userId(data.getUserId())
		.userType(data.getUserType())
		.name(data.getName())
		.user(data.getUser())
		.value(MappingHelper.toJson(data.getValue()))
		.status(data.getStatus());
		
		return builder;
	}
	
}
