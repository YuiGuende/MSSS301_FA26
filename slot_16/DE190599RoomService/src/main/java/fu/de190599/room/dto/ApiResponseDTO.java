package fu.de190599.room.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"status", "message", "data", "timestamp"})
public class ApiResponseDTO<T> {

    public static final int SUCCESS = 1;
    public static final int VALIDATION_FAILED = 2;
    public static final int DUPLICATED = 3;
    public static final int NOT_FOUND = 4;
    public static final int ERROR = 0;

    private int status;
    private String message;
    private T data;
    // ISO 8601: YYYY-MM-DDThh:mm:ssZ
    private String timestamp;

    public static <T> ApiResponseDTO<T> of(int status, String message, T data) {
        return new ApiResponseDTO<>(status, message, data, now());
    }

    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return of(SUCCESS, message, data);
    }

    public static <T> ApiResponseDTO<T> error(int status, String message) {
        return of(status, message, null);
    }

    private static String now() {
        return DateTimeFormatter.ISO_INSTANT.format(Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
