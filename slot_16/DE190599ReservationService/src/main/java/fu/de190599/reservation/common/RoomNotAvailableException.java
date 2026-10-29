package fu.de190599.reservation.common;

import fu.de190599.reservation.dto.ApiResponseDTO;

public class RoomNotAvailableException extends ApiException {

    public RoomNotAvailableException(String message) {
        super(400, ApiResponseDTO.ROOM_NOT_AVAILABLE, message);
    }
}
