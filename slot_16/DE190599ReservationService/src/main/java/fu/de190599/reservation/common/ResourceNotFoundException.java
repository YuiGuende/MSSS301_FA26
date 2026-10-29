package fu.de190599.reservation.common;

import fu.de190599.reservation.dto.ApiResponseDTO;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(404, ApiResponseDTO.NOT_FOUND, message);
    }
}
