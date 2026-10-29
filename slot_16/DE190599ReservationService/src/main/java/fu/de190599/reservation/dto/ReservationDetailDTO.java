package fu.de190599.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fu.de190599.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Reservation with the full room information fetched from Room Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"reservationId", "guestName", "guestEmail", "guestPhone", "checkInDate", "checkOutDate",
        "numberOfGuests", "totalAmount", "status", "room"})
public class ReservationDetailDTO {

    private Long reservationId;
    private String guestName;
    private String guestEmail;
    private String guestPhone;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "01/07/2026")
    private Date checkInDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "03/07/2026")
    private Date checkOutDate;

    private int numberOfGuests;
    private BigDecimal totalAmount;
    private ReservationStatus status;
    private RoomDTO room;
}
