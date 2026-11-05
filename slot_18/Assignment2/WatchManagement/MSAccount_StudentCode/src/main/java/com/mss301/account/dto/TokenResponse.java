package com.mss301.account.dto;

// [TODO-3.4]
public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Integer accountId,
        String username,
        Integer role) {
}
