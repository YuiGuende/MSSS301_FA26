package com.mss301.watch.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// [TODO-6.13] A request rejected before any Saga starts
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
