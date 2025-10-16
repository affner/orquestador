package mx.com.actinver.conf;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandlerConfig {

	private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandlerConfig.class);

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationErrors(HttpServletRequest request,
			MethodArgumentNotValidException e) {
		LOG.error("handleValidationErrors", e);

		HttpStatus status = HttpStatus.BAD_REQUEST;
		String[] messages = getMessages(e);
		Map<String, String> response = handleResponse(status, request, messages);

		return ResponseEntity.status(status).body(response);
	}

	@ExceptionHandler({ InvalidFormatException.class, JsonMappingException.class })
	public ResponseEntity<Map<String, String>> handleInvalidFormatException(HttpServletRequest request,
			JsonMappingException e) {
		LOG.error("handleInvalidFormatException", e);

		HttpStatus status = HttpStatus.BAD_REQUEST;
		String message = "";

		if (e.getMessage().contains("base64")) {
			message = "Base64 invalido.";
		} else {
			message = e.getMessage();
		}

		Map<String, String> response = handleResponse(status, request, message);

		return ResponseEntity.status(status).body(response);
	}

	private String[] getMessages(MethodArgumentNotValidException e) {
		return e.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).toArray(String[]::new);
	}

	private Map<String, String> handleResponse(HttpStatus status, HttpServletRequest request, String... errors) {
		Map<String, String> response = new LinkedHashMap<>();
		response.put("timestamp", LocalDateTime.now().toString());
		response.put("status", String.valueOf(status.value()));
		response.put("error", status.getReasonPhrase());
		response.put("message", String.join("; ", errors));
		response.put("path", request.getRequestURI());

		return response;
	}

}
