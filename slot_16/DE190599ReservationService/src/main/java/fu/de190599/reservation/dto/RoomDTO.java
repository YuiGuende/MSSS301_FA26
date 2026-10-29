package fu.de190599.reservation.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Room data as returned by Room Service.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"roomId", "roomNumber", "roomType", "pricePerNight", "capacity", "floor", "status"})
public class RoomDTO {

    private Long roomId;
    private String roomNumber;
    private RoomType roomType;
    private BigDecimal pricePerNight;
    private int capacity;
    private int floor;
    private RoomStatus status;
}
