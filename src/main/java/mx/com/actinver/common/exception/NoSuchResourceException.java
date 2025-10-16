package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, value = HttpStatus.NOT_FOUND)
public class NoSuchResourceException extends RuntimeException {

	private static final long serialVersionUID = -7752880164172472343L;

	public NoSuchResourceException() {
		super();
	}

	public NoSuchResourceException(String message) {
		super(message);
	}
	
    public NoSuchResourceException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
