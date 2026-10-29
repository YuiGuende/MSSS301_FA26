package fu.de190599.room.common;

import fu.de190599.room.dto.ApiResponseDTO;

public class DuplicateResourceException extends ApiException {

    public DuplicateResourceException(String message) {
        super(226, ApiResponseDTO.DUPLICATED, message);
    }
}
