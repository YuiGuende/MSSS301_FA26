package fu.de190599.room.common;

import lombok.Getter;

/**
 * Business exception carrying both the HTTP code and the ApiResponseDTO status to return.
 */
@Getter
public class ApiException extends RuntimeException {

    private final int httpCode;
    private final int status;

    public ApiException(int httpCode, int status, String message) {
        super(message);
        this.httpCode = httpCode;
        this.status = status;
    }
}
