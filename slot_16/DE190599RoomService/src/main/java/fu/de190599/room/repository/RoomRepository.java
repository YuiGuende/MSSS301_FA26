package fu.de190599.room.repository;

import fu.de190599.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    boolean existsByRoomNumber(String roomNumber);

    boolean existsByRoomNumberAndRoomIdNot(String roomNumber, Long roomId);
}
