package fu.de190599.room.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import fu.de190599.room.common.OnCreate;
import fu.de190599.room.entity.RoomStatus;
import fu.de190599.room.entity.RoomType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
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

/**
 * Request/response body for rooms. "Required" constraints only apply on create (OnCreate group);
 * on update every field is optional, but any provided value must still be valid.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({"roomId", "roomNumber", "roomType", "pricePerNight", "capacity", "floor", "status"})
public class RoomDTO {

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1")
    private Long roomId;

    @NotBlank(groups = OnCreate.class, message = "Room number is required")
    @Pattern(regexp = "^\\S(.*\\S)?$", message = "Room number must not be blank or start/end with spaces")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    @Schema(example = "A-101")
    private String roomNumber;

    @NotNull(groups = OnCreate.class, message = "Room type is required (SINGLE, DOUBLE, SUITE, DELUXE)")
    @Schema(example = "DOUBLE")
    private RoomType roomType;

    @NotNull(groups = OnCreate.class, message = "Price per night is required")
    @DecimalMin(value = "0", inclusive = false, message = "Price per night must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Price per night must have at most 16 integer digits and 2 decimal places")
    @Schema(example = "850000")
    private BigDecimal pricePerNight;

    @NotNull(groups = OnCreate.class, message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be between 1 and 10")
    @Max(value = 10, message = "Capacity must be between 1 and 10")
    @Schema(example = "2")
    private Integer capacity;

    @NotNull(groups = OnCreate.class, message = "Floor is required")
    @Min(value = 1, message = "Floor must be greater than or equal to 1")
    @Schema(example = "1")
    private Integer floor;

    @Schema(example = "AVAILABLE", description = "Defaults to AVAILABLE on create")
    private RoomStatus status;
}
