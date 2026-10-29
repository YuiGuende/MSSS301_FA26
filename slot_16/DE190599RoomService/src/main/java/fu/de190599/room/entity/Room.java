package fu.de190599.room.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_number", length = 20, nullable = false, unique = true)
    private String roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", length = 10, nullable = false)
    private RoomType roomType;

    @Column(name = "price_per_night", precision = 18, scale = 2, nullable = false)
    private BigDecimal pricePerNight;

    @Column(name = "capacity", nullable = false)
    private int capacity;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private RoomStatus status;
}
