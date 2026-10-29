package fu.de190599.reservation.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class JacksonConfig {

    /**
     * Jackson formats java.util.Date in UTC by default, while dates read from the DATE columns are at
     * local midnight. Using the JVM zone keeps "dd/MM/yyyy" from shifting by one day.
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localTimeZoneCustomizer() {
        return builder -> builder.timeZone(TimeZone.getDefault());
    }
}
