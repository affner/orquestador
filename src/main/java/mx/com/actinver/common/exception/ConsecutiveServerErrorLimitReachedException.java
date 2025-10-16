package mx.com.actinver.common.exception;

public class ConsecutiveServerErrorLimitReachedException extends RuntimeException {
    public ConsecutiveServerErrorLimitReachedException(String message) {
        super(message);
    }
}
