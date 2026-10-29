package fu.de190599.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.OptBoolean;
import fu.de190599.reservation.common.OnCreate;
import fu.de190599.reservation.entity.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Request/response body for reservations. "Required" constraints only apply on create (OnCreate group);
 * on update every field is optional, but any provided value must still be valid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"reservationId", "guestName", "guestEmail", "guestPhone", "checkInDate", "checkOutDate",
        "numberOfGuests", "totalAmount", "status", "roomId"})
public class ReservationDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long reservationId;

    @NotBlank(groups = OnCreate.class, message = "Guest name is required")
    @Pattern(regexp = "^\\S(.*\\S)?$", message = "Guest name must not be blank or start/end with spaces")
    @Size(max = 100, message = "Guest name must not exceed 100 characters")
    @Schema(example = "Nguyen Van A")
    private String guestName;

    @NotBlank(groups = OnCreate.class, message = "Guest email is required")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$",
            message = "Guest email is invalid")
    @Size(max = 100, message = "Guest email must not exceed 100 characters")
    @Schema(example = "nguyenvana@gmail.com")
    private String guestEmail;

    @NotBlank(groups = OnCreate.class, message = "Guest phone is required")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Guest phone must contain 8-15 digits and may start with +")
    @Size(max = 20, message = "Guest phone must not exceed 20 characters")
    @Schema(example = "0901234567")
    private String guestPhone;

    @NotNull(groups = OnCreate.class, message = "Check-in date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", lenient = OptBoolean.FALSE)
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "01/07/2026")
    private Date checkInDate;

    @NotNull(groups = OnCreate.class, message = "Check-out date is required")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy", lenient = OptBoolean.FALSE)
    @Schema(type = "string", pattern = "dd/MM/yyyy", example = "03/07/2026", description = "Must be after checkInDate")
    private Date checkOutDate;

    @NotNull(groups = OnCreate.class, message = "Number of guests is required")
    @Min(value = 1, message = "Number of guests must be between 1 and 10")
    @Max(value = 10, message = "Number of guests must be between 1 and 10")
    @Schema(example = "2", description = "Must not exceed the room capacity")
    private Integer numberOfGuests;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Computed = pricePerNight x number of nights")
    private BigDecimal totalAmount;

    @Schema(example = "CONFIRMED", description = "Always CONFIRMED on create; can be changed on update")
    private ReservationStatus status;

    @NotNull(groups = OnCreate.class, message = "Room id is required")
    @Min(value = 1, message = "Room id must be a positive number")
    @Schema(example = "1")
    private Long roomId;
}
