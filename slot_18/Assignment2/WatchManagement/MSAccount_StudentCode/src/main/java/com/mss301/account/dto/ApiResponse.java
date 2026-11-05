package com.mss301.account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// [TODO-3.4] Common response envelope; data is omitted when absent
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
