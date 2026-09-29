package org.example.constant;

public final class HeaderConstants {
    private HeaderConstants() {}

    public static final String X_CORRELATION_ID = "X-Correlation-Id"; // Mã traceId cho hệ thống log
    public static final String X_USER_ID = "X-User-Id";               // Truyền ID của user sau khi Gateway đã verify JWT
    public static final String X_USER_ROLE = "X-User-Role";
    public static final String AUTHORIZATION = "Authorization";
}