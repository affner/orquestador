package mx.com.actinver.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

	private static final long serialVersionUID = 5367627256323481891L;

	public NotFoundException() {
		super();
	}

	public NotFoundException(String message) {
		super(message);
	}

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
