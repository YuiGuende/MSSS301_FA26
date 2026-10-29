package fu.de190599.room.common;

import fu.de190599.room.dto.ApiResponseDTO;

public class ValidationFailedException extends ApiException {

    public ValidationFailedException(String message) {
        super(406, ApiResponseDTO.VALIDATION_FAILED, "Data validation failed. " + message);
    }
}
