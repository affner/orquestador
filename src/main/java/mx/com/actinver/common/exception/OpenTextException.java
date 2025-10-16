package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, value = HttpStatus.INTERNAL_SERVER_ERROR)
public class OpenTextException extends RuntimeException {

	private static final long serialVersionUID = 1038007548643535462L;

	public OpenTextException() {
		super();
	}

	public OpenTextException(String message) {
		super(message);
	}

	public OpenTextException(String message, Throwable cause) {
		super(message, cause);
	}

}
