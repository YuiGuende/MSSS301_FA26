package fu.de190599.room.common;

import fu.de190599.room.dto.ApiResponseDTO;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(404, ApiResponseDTO.NOT_FOUND, message);
    }
}
