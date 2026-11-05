package com.mss301.account.dto;

import jakarta.validation.constraints.NotBlank;

// [TODO-3.4] Missing fields are rejected with HTTP 400
public record LoginRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password) {
}
