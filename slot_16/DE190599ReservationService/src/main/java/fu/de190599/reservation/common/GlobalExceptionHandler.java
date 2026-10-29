package fu.de190599.reservation.common;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import fu.de190599.reservation.dto.ApiResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final int NOT_ACCEPTABLE = 406;

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleApi(ApiException ex) {
        return build(ex.getHttpCode(), ex.getStatus(), ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleBodyValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return validationFailed(message);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodValidation(HandlerMethodValidationException ex) {
        String message = ex.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return validationFailed(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleConstraintViolation(ConstraintViolationException ex) {
        String message = ex.getConstraintViolations().stream()
                .map(v -> v.getMessage())
                .collect(Collectors.joining("; "));
        return validationFailed(message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleNotReadable(HttpMessageNotReadableException ex) {
        String message = "Request body is missing or malformed";
        if (ex.getCause() instanceof InvalidFormatException ife && !ife.getPath().isEmpty()) {
            String field = ife.getPath().get(ife.getPath().size() - 1).getFieldName();
            message = "Invalid value '" + ife.getValue() + "' for field '" + field + "'";
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                message += ". Accepted values: " + Arrays.toString(ife.getTargetType().getEnumConstants());
            } else if (ife.getTargetType() != null && Date.class.isAssignableFrom(ife.getTargetType())) {
                message += ". The accepted format is dd/MM/yyyy";
            }
        } else if (ex.getCause() instanceof MismatchedInputException mie && !mie.getPath().isEmpty()) {
            message = "Invalid data type for field '" + mie.getPath().get(mie.getPath().size() - 1).getFieldName() + "'";
        }
        return validationFailed(message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        Class<?> type = ex.getRequiredType();
        if (type != null && type.isEnum()) {
            message += ". Accepted values: " + Arrays.toString(type.getEnumConstants());
        }
        return validationFailed(message);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return validationFailed("Parameter '" + ex.getParameterName() + "' is required");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMediaType(HttpMediaTypeNotSupportedException ex) {
        return validationFailed("Content-Type must be application/json");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleNoResource(NoResourceFoundException ex) {
        return build(404, ApiResponseDTO.NOT_FOUND, "Resource not found: /" + ex.getResourcePath());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(405, ApiResponseDTO.ERROR, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDTO<Object>> handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error on {} {}", request.getMethod(), request.getRequestURI(), ex);
        String message = "Internal server error";
        if (request.getAttribute(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE) instanceof HandlerMethod hm) {
            FailureMessage failure = hm.getMethodAnnotation(FailureMessage.class);
            if (failure != null) {
                message = "Internal server error. " + failure.value();
            }
        }
        return build(500, ApiResponseDTO.ERROR, message);
    }

    private ResponseEntity<ApiResponseDTO<Object>> validationFailed(String message) {
        return build(NOT_ACCEPTABLE, ApiResponseDTO.VALIDATION_FAILED, "Data validation failed. " + message);
    }

    private ResponseEntity<ApiResponseDTO<Object>> build(int httpCode, int status, String message) {
        return ResponseEntity.status(httpCode).body(ApiResponseDTO.error(status, message));
    }
}
