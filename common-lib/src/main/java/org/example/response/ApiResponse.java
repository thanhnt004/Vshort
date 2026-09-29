package org.example.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private String code;
    private String message;
    private T data;
    private String traceId;
    @Builder.Default
    private long timestamp = Instant.now().toEpochMilli();


    public ApiResponse() {
    }
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("Operation successful")
                .data(data)
                .success(true)
                // ramdom for demo
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    public static <T> ApiResponse<T> success() {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message("Operation successful")
                .success(true)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    public static <T> ApiResponse<T> error(BaseErrorCode errorCode) {
        return ApiResponse.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .success(false)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .success(false)
                .traceId(UUID.randomUUID().toString())
                .build();
    }
}
