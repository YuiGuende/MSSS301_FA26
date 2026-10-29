package fu.de190599.reservation.service.impl;

import feign.FeignException;
import fu.de190599.reservation.common.ResourceNotFoundException;
import fu.de190599.reservation.common.RoomNotAvailableException;
import fu.de190599.reservation.common.ValidationFailedException;
import fu.de190599.reservation.dto.ApiResponseDTO;
import fu.de190599.reservation.dto.PageDTO;
import fu.de190599.reservation.dto.ReservationDTO;
import fu.de190599.reservation.dto.ReservationDetailDTO;
import fu.de190599.reservation.dto.RoomDTO;
import fu.de190599.reservation.dto.RoomStatus;
import fu.de190599.reservation.entity.Reservation;
import fu.de190599.reservation.entity.ReservationStatus;
import fu.de190599.reservation.repository.ReservationRepository;
import fu.de190599.reservation.service.ReservationService;
import fu.de190599.reservation.service.RoomServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ReservationRepository reservationRepository;
    private final RoomServiceClient roomServiceClient;

    @Override
    @Transactional
    public ReservationDTO createReservation(ReservationDTO dto) {
        RoomDTO room = fetchRoom(dto.getRoomId());
        requireAvailable(room);
        validateStay(dto.getCheckInDate(), dto.getCheckOutDate(), dto.getNumberOfGuests(), room);

        Reservation reservation = Reservation.builder()
                .guestName(dto.getGuestName())
                .guestEmail(dto.getGuestEmail())
                .guestPhone(dto.getGuestPhone())
                .checkInDate(dto.getCheckInDate())
                .checkOutDate(dto.getCheckOutDate())
                .numberOfGuests(dto.getNumberOfGuests())
                .totalAmount(computeTotal(room, dto.getCheckInDate(), dto.getCheckOutDate()))
                .status(ReservationStatus.CONFIRMED)
                .roomId(room.getRoomId())
                .build();
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional
    public ReservationDTO updateReservation(Long reservationId, ReservationDTO dto) {
        Reservation reservation = findReservation(reservationId);

        if (dto.getGuestName() != null) {
            reservation.setGuestName(dto.getGuestName());
        }
        if (dto.getGuestEmail() != null) {
            reservation.setGuestEmail(dto.getGuestEmail());
        }
        if (dto.getGuestPhone() != null) {
            reservation.setGuestPhone(dto.getGuestPhone());
        }
        if (dto.getStatus() != null) {
            reservation.setStatus(dto.getStatus());
        }

        boolean roomChanged = dto.getRoomId() != null && !Objects.equals(dto.getRoomId(), reservation.getRoomId());
        boolean stayChanged = roomChanged || dto.getCheckInDate() != null || dto.getCheckOutDate() != null
                || dto.getNumberOfGuests() != null;

        if (stayChanged) {
            Date checkIn = dto.getCheckInDate() != null ? dto.getCheckInDate() : reservation.getCheckInDate();
            Date checkOut = dto.getCheckOutDate() != null ? dto.getCheckOutDate() : reservation.getCheckOutDate();
            int guests = dto.getNumberOfGuests() != null ? dto.getNumberOfGuests() : reservation.getNumberOfGuests();

            RoomDTO room = fetchRoom(roomChanged ? dto.getRoomId() : reservation.getRoomId());
            if (roomChanged) {
                requireAvailable(room);
            }
            validateStay(checkIn, checkOut, guests, room);

            reservation.setRoomId(room.getRoomId());
            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setNumberOfGuests(guests);
            reservation.setTotalAmount(computeTotal(room, checkIn, checkOut));
        }
        return toDTO(reservationRepository.save(reservation));
    }

    @Override
    @Transactional(readOnly = true)
    public ReservationDetailDTO getReservationById(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        return toDetailDTO(reservation, findRoomOrNull(reservation.getRoomId()));
    }

    @Override
    @Transactional
    public void cancelReservation(Long reservationId) {
        Reservation reservation = findReservation(reservationId);
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public PageDTO<ReservationDetailDTO> getReservations(int page, int size, String guestName,
                                                         ReservationStatus status) {
        if (page < 0) {
            throw new ValidationFailedException("Page number must be greater than or equal to 0");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ValidationFailedException("Page size must be between 1 and " + MAX_PAGE_SIZE);
        }

        Specification<Reservation> spec = (root, query, cb) -> cb.conjunction();
        if (guestName != null && !guestName.isBlank()) {
            String keyword = "%" + guestName.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("guestName")), keyword));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        Page<Reservation> result = reservationRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("reservationId")));

        // Several reservations may share a room: call Room Service once per room
        Map<Long, RoomDTO> rooms = new HashMap<>();
        List<ReservationDetailDTO> content = result.getContent().stream()
                .map(r -> toDetailDTO(r, rooms.computeIfAbsent(r.getRoomId(), this::findRoomOrNull)))
                .toList();
        return PageDTO.of(result, content);
    }

    private Reservation findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation is not found with id: " + reservationId));
    }

    private RoomDTO fetchRoom(Long roomId) {
        try {
            ApiResponseDTO<RoomDTO> response = roomServiceClient.getRoomById(roomId);
            if (response == null || response.getData() == null) {
                throw new ResourceNotFoundException("Room is not found with id: " + roomId);
            }
            return response.getData();
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Room is not found with id: " + roomId);
        }
    }

    // Detail views still return the reservation if Room Service cannot provide the room
    private RoomDTO findRoomOrNull(Long roomId) {
        try {
            return fetchRoom(roomId);
        } catch (RuntimeException e) {
            log.warn("Cannot get room {} from Room Service: {}", roomId, e.getMessage());
            return null;
        }
    }

    private void requireAvailable(RoomDTO room) {
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new RoomNotAvailableException("Room " + room.getRoomId() + " is not AVAILABLE for reservation"
                    + " (current status: " + room.getStatus() + ")");
        }
    }

    private void validateStay(Date checkIn, Date checkOut, int guests, RoomDTO room) {
        if (!toLocalDate(checkOut).isAfter(toLocalDate(checkIn))) {
            throw new ValidationFailedException("Check-out date must be after check-in date");
        }
        if (guests > room.getCapacity()) {
            throw new ValidationFailedException("Number of guests (" + guests + ") exceeds the room capacity ("
                    + room.getCapacity() + ")");
        }
    }

    private BigDecimal computeTotal(RoomDTO room, Date checkIn, Date checkOut) {
        long nights = ChronoUnit.DAYS.between(toLocalDate(checkIn), toLocalDate(checkOut));
        return room.getPricePerNight().multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }

    // java.sql.Date (loaded by JPA) does not support toInstant(), so handle it separately
    private static LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static Date toUtilDate(Date date) {
        return date == null ? null : new Date(date.getTime());
    }

    private ReservationDTO toDTO(Reservation r) {
        return ReservationDTO.builder()
                .reservationId(r.getReservationId())
                .guestName(r.getGuestName())
                .guestEmail(r.getGuestEmail())
                .guestPhone(r.getGuestPhone())
                .checkInDate(toUtilDate(r.getCheckInDate()))
                .checkOutDate(toUtilDate(r.getCheckOutDate()))
                .numberOfGuests(r.getNumberOfGuests())
                .totalAmount(r.getTotalAmount())
                .status(r.getStatus())
                .roomId(r.getRoomId())
                .build();
    }

    private ReservationDetailDTO toDetailDTO(Reservation r, RoomDTO room) {
        return ReservationDetailDTO.builder()
                .reservationId(r.getReservationId())
                .guestName(r.getGuestName())
                .guestEmail(r.getGuestEmail())
                .guestPhone(r.getGuestPhone())
                .checkInDate(toUtilDate(r.getCheckInDate()))
                .checkOutDate(toUtilDate(r.getCheckOutDate()))
                .numberOfGuests(r.getNumberOfGuests())
                .totalAmount(r.getTotalAmount())
                .status(r.getStatus())
                .room(room)
                .build();
    }
}
