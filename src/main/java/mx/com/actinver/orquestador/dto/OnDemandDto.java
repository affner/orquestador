package mx.com.actinver.orquestador.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(Include.NON_NULL)
public class OnDemandDto<T> {

	private Request<T> request;

	private Response response;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class Request<T> {

		private Long templateId;

		private String templateVersion;

		private T target;

		private Long executor;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	@Builder
	@JsonInclude(Include.NON_NULL)
	public static class Response {

		private String processId;

		private Integer progress;

		private String message;

		private List<CtrlNumberDto> result;

		private List<File> files;

		@Data
		@AllArgsConstructor
		@NoArgsConstructor
		@Builder
		@JsonInclude(Include.NON_NULL)
		public static class File {

			private String path;

		}
	}

}
