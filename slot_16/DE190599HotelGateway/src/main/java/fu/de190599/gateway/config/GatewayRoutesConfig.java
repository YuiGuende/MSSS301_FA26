package fu.de190599.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Configuration
public class GatewayRoutesConfig {

    @Value("${services.room.url}")
    private String roomServiceUrl;

    @Value("${services.reservation.url}")
    private String reservationServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> roomServiceRoute() {
        return route("room-service")
                .route(path("/api/rooms", "/api/rooms/**"), http())
                .before(uri(roomServiceUrl))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reservationServiceRoute() {
        return route("reservation-service")
                .route(path("/api/reservations", "/api/reservations/**"), http())
                .before(uri(reservationServiceUrl))
                .build();
    }

    // /room-service/v3/api-docs -> Room Service /v3/api-docs (used by the gateway Swagger UI)
    @Bean
    public RouterFunction<ServerResponse> roomServiceApiDocsRoute() {
        return route("room-service-api-docs")
                .route(path("/room-service/v3/api-docs"), http())
                .before(uri(roomServiceUrl))
                .before(stripPrefix(1))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> reservationServiceApiDocsRoute() {
        return route("reservation-service-api-docs")
                .route(path("/reservation-service/v3/api-docs"), http())
                .before(uri(reservationServiceUrl))
                .before(stripPrefix(1))
                .build();
    }
}
