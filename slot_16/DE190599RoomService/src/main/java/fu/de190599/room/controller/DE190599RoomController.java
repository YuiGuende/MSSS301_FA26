package fu.de190599.room.controller;

import fu.de190599.room.common.FailureMessage;
import fu.de190599.room.common.OnCreate;
import fu.de190599.room.dto.ApiResponseDTO;
import fu.de190599.room.dto.PageDTO;
import fu.de190599.room.dto.RoomDTO;
import fu.de190599.room.entity.RoomStatus;
import fu.de190599.room.entity.RoomType;
import fu.de190599.room.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/rooms", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Room", description = "Room management APIs")
public class DE190599RoomController {

    private final RoomService roomService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new room in the system")
    @FailureMessage("Failed to create room")
    public ResponseEntity<ApiResponseDTO<RoomDTO>> createRoom(
            @Validated({OnCreate.class, Default.class}) @RequestBody RoomDTO roomDTO) {
        RoomDTO created = roomService.createRoom(roomDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Room created successfully", created));
    }

    @PutMapping(value = "/{roomId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a room by room Id (partial update)")
    @FailureMessage("Failed to update room")
    public ResponseEntity<ApiResponseDTO<RoomDTO>> updateRoom(
            @PathVariable Long roomId,
            @Validated(Default.class) @RequestBody RoomDTO roomDTO) {
        RoomDTO updated = roomService.updateRoom(roomId, roomDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Room updated successfully", updated));
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get details of a room by room Id")
    @FailureMessage("Failed to get room")
    public ResponseEntity<ApiResponseDTO<RoomDTO>> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponseDTO.success("Get room successfully", roomService.getRoomById(roomId)));
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Set a room to MAINTENANCE status by room Id")
    @FailureMessage("Failed to update room status")
    public ResponseEntity<ApiResponseDTO<Object>> setRoomToMaintenance(@PathVariable Long roomId) {
        roomService.setRoomToMaintenance(roomId);
        return ResponseEntity.ok(ApiResponseDTO.success("Room set to MAINTENANCE successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of rooms, optionally filtered by room type and status")
    @FailureMessage("Failed to get rooms")
    public ResponseEntity<ApiResponseDTO<PageDTO<RoomDTO>>> getRooms(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) RoomType roomType,
            @RequestParam(required = false) RoomStatus status) {
        PageDTO<RoomDTO> rooms = roomService.getRooms(page, size, roomType, status);
        return ResponseEntity.ok(ApiResponseDTO.success("Get rooms successfully", rooms));
    }
}
