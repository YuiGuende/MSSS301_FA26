package fu.de190599.reservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI reservationServiceOpenAPI(@Value("${gateway.url:http://localhost:8080}") String gatewayUrl,
                                             @Value("${server.port:8082}") int port) {
        return new OpenAPI()
                .info(new Info()
                        .title("DE190599 Reservation Service API")
                        .description("Reservation management - MSS301 Practical Exam")
                        .version("1.0"))
                // Gateway first so "Try it out" from the gateway's Swagger UI goes through the gateway
                .servers(List.of(
                        new Server().url(gatewayUrl).description("Hotel Gateway"),
                        new Server().url("http://localhost:" + port).description("Reservation Service (direct)")));
    }
}
