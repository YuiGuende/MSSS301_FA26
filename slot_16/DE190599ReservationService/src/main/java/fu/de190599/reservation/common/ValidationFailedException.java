package fu.de190599.reservation.common;

import fu.de190599.reservation.dto.ApiResponseDTO;

public class ValidationFailedException extends ApiException {

    public ValidationFailedException(String message) {
        super(406, ApiResponseDTO.VALIDATION_FAILED, "Data validation failed. " + message);
    }
}
