package fu.de190599.reservation.controller;

import fu.de190599.reservation.common.FailureMessage;
import fu.de190599.reservation.common.OnCreate;
import fu.de190599.reservation.dto.ApiResponseDTO;
import fu.de190599.reservation.dto.PageDTO;
import fu.de190599.reservation.dto.ReservationDTO;
import fu.de190599.reservation.dto.ReservationDetailDTO;
import fu.de190599.reservation.entity.ReservationStatus;
import fu.de190599.reservation.service.ReservationService;
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
@RequestMapping(value = "/api/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Reservation", description = "Reservation management APIs")
public class DE190599ReservationController {

    private final ReservationService reservationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new reservation (room must exist and be AVAILABLE)")
    @FailureMessage("Failed to create reservation")
    public ResponseEntity<ApiResponseDTO<ReservationDTO>> createReservation(
            @Validated({OnCreate.class, Default.class}) @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO created = reservationService.createReservation(reservationDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success("Reservation created successfully", created));
    }

    @PutMapping(value = "/{reservationId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a reservation by reservation Id (partial update)")
    @FailureMessage("Failed to update reservation")
    public ResponseEntity<ApiResponseDTO<ReservationDTO>> updateReservation(
            @PathVariable Long reservationId,
            @Validated(Default.class) @RequestBody ReservationDTO reservationDTO) {
        ReservationDTO updated = reservationService.updateReservation(reservationId, reservationDTO);
        return ResponseEntity.ok(ApiResponseDTO.success("Reservation updated successfully", updated));
    }

    @GetMapping("/{reservationId}")
    @Operation(summary = "Get details of a reservation by reservation Id")
    @FailureMessage("Failed to get reservation")
    public ResponseEntity<ApiResponseDTO<ReservationDetailDTO>> getReservation(@PathVariable Long reservationId) {
        ReservationDetailDTO detail = reservationService.getReservationById(reservationId);
        return ResponseEntity.ok(ApiResponseDTO.success("Get reservation successfully", detail));
    }

    @DeleteMapping("/{reservationId}")
    @Operation(summary = "Cancel a reservation by reservation Id")
    @FailureMessage("Failed to cancel reservation")
    public ResponseEntity<ApiResponseDTO<Object>> cancelReservation(@PathVariable Long reservationId) {
        reservationService.cancelReservation(reservationId);
        return ResponseEntity.ok(ApiResponseDTO.success("Reservation cancelled successfully", null));
    }

    @GetMapping
    @Operation(summary = "Get a paginated list of reservations, optionally filtered by guest name and status")
    @FailureMessage("Failed to get reservations")
    public ResponseEntity<ApiResponseDTO<PageDTO<ReservationDetailDTO>>> getReservations(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String guestName,
            @RequestParam(required = false) ReservationStatus status) {
        PageDTO<ReservationDetailDTO> reservations = reservationService.getReservations(page, size, guestName, status);
        return ResponseEntity.ok(ApiResponseDTO.success("Get reservations successfully", reservations));
    }
}
