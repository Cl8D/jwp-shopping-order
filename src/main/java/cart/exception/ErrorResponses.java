package cart.exception;

import java.util.List;

public class ErrorResponses {

    private final ErrorCode errorCode;
    private final List<String> errorMessage;

    public ErrorResponses(final ErrorCode errorCode, final List<String> errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public List<String> getErrorMessage() {
        return errorMessage;
    }
}
