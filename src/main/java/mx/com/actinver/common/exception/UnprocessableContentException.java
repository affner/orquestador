package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, value = HttpStatus.UNPROCESSABLE_ENTITY)
public class UnprocessableContentException extends RuntimeException {

	private static final long serialVersionUID = -2489880585733039370L;

	public UnprocessableContentException() {
		super();
	}

	public UnprocessableContentException(String message) {
		super(message);
	}

	public UnprocessableContentException(String message, Throwable cause) {
		super(message, cause);
	}

}
