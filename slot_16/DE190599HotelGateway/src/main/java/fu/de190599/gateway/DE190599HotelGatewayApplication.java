package fu.de190599.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// No user store needed: the gateway only routes requests and applies CORS
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class DE190599HotelGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DE190599HotelGatewayApplication.class, args);
    }
}
