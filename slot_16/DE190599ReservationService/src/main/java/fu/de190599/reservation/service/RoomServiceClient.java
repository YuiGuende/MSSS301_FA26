package fu.de190599.reservation.service;

import fu.de190599.reservation.dto.ApiResponseDTO;
import fu.de190599.reservation.dto.RoomDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OpenFeign client for Room Service. A 404 from Room Service surfaces as FeignException.NotFound.
 */
@FeignClient(name = "room-service", url = "${room-service.url}")
public interface RoomServiceClient {

    @GetMapping("/api/rooms/{roomId}")
    ApiResponseDTO<RoomDTO> getRoomById(@PathVariable("roomId") Long roomId);
}
