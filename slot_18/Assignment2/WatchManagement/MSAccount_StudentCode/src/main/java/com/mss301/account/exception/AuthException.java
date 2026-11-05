package com.mss301.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// [TODO-3.4] Authentication failure with the HTTP status it maps to
@Getter
public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
