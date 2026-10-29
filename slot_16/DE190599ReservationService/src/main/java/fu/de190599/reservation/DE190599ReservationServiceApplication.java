package fu.de190599.reservation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DE190599ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DE190599ReservationServiceApplication.class, args);
    }
}
