package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, value = HttpStatus.BAD_REQUEST)
public class InvalidParamException extends RuntimeException {

	private static final long serialVersionUID = -4676945701630713648L;

	public InvalidParamException() {
		super();
	}

	public InvalidParamException(String message) {
		super(message);
	}
	
}
