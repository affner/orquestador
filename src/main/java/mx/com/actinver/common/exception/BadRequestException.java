package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

	private static final long serialVersionUID = 5297731296910203814L;

	public BadRequestException() {
		super();
	}

	public BadRequestException(String message) {
		super(message);
	}
	
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
