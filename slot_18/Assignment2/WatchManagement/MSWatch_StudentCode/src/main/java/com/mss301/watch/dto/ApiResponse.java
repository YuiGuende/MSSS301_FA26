package com.mss301.watch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// [TODO-6.14] Common response envelope; data is omitted when absent
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String message, T data) {

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
