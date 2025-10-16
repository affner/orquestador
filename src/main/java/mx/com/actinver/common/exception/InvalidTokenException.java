package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, value = HttpStatus.UNAUTHORIZED)
public class InvalidTokenException extends RuntimeException {

	private static final long serialVersionUID = -19248073294546403L;

	public InvalidTokenException() {
		super();
	}

	public InvalidTokenException(String message) {
		super(message);
	}
	
	public InvalidTokenException(String message, Throwable cause) {
		super(message, cause);
	}
	
}
