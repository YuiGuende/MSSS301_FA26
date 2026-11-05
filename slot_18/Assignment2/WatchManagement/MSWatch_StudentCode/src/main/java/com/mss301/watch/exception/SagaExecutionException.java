package com.mss301.watch.exception;

import com.mss301.watch.service.saga.SagaStatus;
import lombok.Getter;
import org.springframework.http.HttpStatus;

// [TODO-6.15] A Saga that did not complete, with what the client needs to know about it
@Getter
public class SagaExecutionException extends RuntimeException {

    private final String sagaId;
    private final SagaStatus sagaStatus;
    private final String failedStep;
    private final String reason;
    private final HttpStatus httpStatus;

    public SagaExecutionException(String sagaId, SagaStatus sagaStatus, String failedStep, String reason, HttpStatus httpStatus) {
        super("Saga " + sagaStatus + ": " + reason);
        this.sagaId = sagaId;
        this.sagaStatus = sagaStatus;
        this.failedStep = failedStep;
        this.reason = reason;
        this.httpStatus = httpStatus;
    }
}
