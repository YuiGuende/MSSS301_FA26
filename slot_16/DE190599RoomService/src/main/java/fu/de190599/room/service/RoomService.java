package fu.de190599.room.service;

import fu.de190599.room.dto.PageDTO;
import fu.de190599.room.dto.RoomDTO;
import fu.de190599.room.entity.RoomStatus;
import fu.de190599.room.entity.RoomType;

public interface RoomService {

    RoomDTO createRoom(RoomDTO roomDTO);

    RoomDTO updateRoom(Long roomId, RoomDTO roomDTO);

    RoomDTO getRoomById(Long roomId);

    void setRoomToMaintenance(Long roomId);

    PageDTO<RoomDTO> getRooms(int page, int size, RoomType roomType, RoomStatus status);
}
