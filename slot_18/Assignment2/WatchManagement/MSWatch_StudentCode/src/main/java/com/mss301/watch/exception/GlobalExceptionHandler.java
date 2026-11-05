package com.mss301.watch.exception;

import com.mss301.watch.dto.ApiResponse;
import com.mss301.watch.dto.SagaFailureResponse;
import com.mss301.watch.dto.WatchSagaResponse;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // [TODO-6.15] A failed Saga reports its id, final status and failed step
    @ExceptionHandler(SagaExecutionException.class)
    public ResponseEntity<ApiResponse<SagaFailureResponse>> handleSaga(SagaExecutionException ex) {
        SagaFailureResponse data = new SagaFailureResponse(ex.getSagaId(), ex.getSagaStatus(), ex.getFailedStep(), ex.getReason());
        return ResponseEntity.status(ex.getHttpStatus())
                .header(WatchSagaResponse.SAGA_HEADER, ex.getSagaId())
                .body(ApiResponse.error(ex.getMessage(), data));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApi(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(ApiResponse.error(message, null));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Malformed request body", null));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Invalid value for " + ex.getName(), null));
    }
}
