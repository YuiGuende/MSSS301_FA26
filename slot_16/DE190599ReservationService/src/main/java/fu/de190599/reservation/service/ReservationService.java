package fu.de190599.reservation.service;

import fu.de190599.reservation.dto.PageDTO;
import fu.de190599.reservation.dto.ReservationDTO;
import fu.de190599.reservation.dto.ReservationDetailDTO;
import fu.de190599.reservation.entity.ReservationStatus;

public interface ReservationService {

    ReservationDTO createReservation(ReservationDTO reservationDTO);

    ReservationDTO updateReservation(Long reservationId, ReservationDTO reservationDTO);

    ReservationDetailDTO getReservationById(Long reservationId);

    void cancelReservation(Long reservationId);

    PageDTO<ReservationDetailDTO> getReservations(int page, int size, String guestName, ReservationStatus status);
}
