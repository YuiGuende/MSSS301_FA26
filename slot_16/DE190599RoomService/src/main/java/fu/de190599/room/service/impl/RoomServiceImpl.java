package fu.de190599.room.service.impl;

import fu.de190599.room.common.DuplicateResourceException;
import fu.de190599.room.common.ResourceNotFoundException;
import fu.de190599.room.common.ValidationFailedException;
import fu.de190599.room.dto.PageDTO;
import fu.de190599.room.dto.RoomDTO;
import fu.de190599.room.entity.Room;
import fu.de190599.room.entity.RoomStatus;
import fu.de190599.room.entity.RoomType;
import fu.de190599.room.repository.RoomRepository;
import fu.de190599.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private static final int MAX_PAGE_SIZE = 100;

    private final RoomRepository roomRepository;

    @Override
    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        String roomNumber = dto.getRoomNumber();
        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new DuplicateResourceException("Room number '" + roomNumber + "' is duplicated");
        }
        Room room = Room.builder()
                .roomNumber(roomNumber)
                .roomType(dto.getRoomType())
                .pricePerNight(dto.getPricePerNight())
                .capacity(dto.getCapacity())
                .floor(dto.getFloor())
                .status(dto.getStatus() != null ? dto.getStatus() : RoomStatus.AVAILABLE)
                .build();
        return toDTO(roomRepository.save(room));
    }

    @Override
    @Transactional
    public RoomDTO updateRoom(Long roomId, RoomDTO dto) {
        Room room = findRoom(roomId);

        if (dto.getRoomNumber() != null) {
            if (roomRepository.existsByRoomNumberAndRoomIdNot(dto.getRoomNumber(), roomId)) {
                throw new DuplicateResourceException("Room number '" + dto.getRoomNumber() + "' is duplicated");
            }
            room.setRoomNumber(dto.getRoomNumber());
        }
        if (dto.getRoomType() != null) {
            room.setRoomType(dto.getRoomType());
        }
        if (dto.getPricePerNight() != null) {
            room.setPricePerNight(dto.getPricePerNight());
        }
        if (dto.getCapacity() != null) {
            room.setCapacity(dto.getCapacity());
        }
        if (dto.getFloor() != null) {
            room.setFloor(dto.getFloor());
        }
        if (dto.getStatus() != null) {
            room.setStatus(dto.getStatus());
        }
        return toDTO(roomRepository.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long roomId) {
        return toDTO(findRoom(roomId));
    }

    @Override
    @Transactional
    public void setRoomToMaintenance(Long roomId) {
        Room room = findRoom(roomId);
        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<RoomDTO> getRooms(int page, int size, RoomType roomType, RoomStatus status) {
        if (page < 0) {
            throw new ValidationFailedException("Page number must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationFailedException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        Specification<Room> spec = (root, query, cb) -> cb.conjunction();
        if (roomType != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("roomType"), roomType));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Room> result = roomRepository.findAll(spec, PageRequest.of(page, size, Sort.by("roomId")));
        return PageDTO.of(result, result.getContent().stream().map(this::toDTO).toList());
    }

    private Room findRoom(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room is not found with id: " + roomId));
    }

    private RoomDTO toDTO(Room room) {
        return RoomDTO.builder()
                .roomId(room.getRoomId())
                .roomNumber(room.getRoomNumber())
                .roomType(room.getRoomType())
                .pricePerNight(room.getPricePerNight())
                .capacity(room.getCapacity())
                .floor(room.getFloor())
                .status(room.getStatus())
                .build();
    }
}
