package com.bitdance.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.slf4j.MDC;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(String code, String message, T data, String traceId) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>("SUCCESS", "success", data, MDC.get("traceId"));
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>("SUCCESS", "success", null, MDC.get("traceId"));
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        return new ApiResponse<>(code, message, null, MDC.get("traceId"));
    }
}
