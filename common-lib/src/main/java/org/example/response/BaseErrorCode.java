package org.example.response;

public interface BaseErrorCode {
    String getCode();
    String getMessage();
    int getStatusCode();
    default String formatMessage(Object... args) {
        return String.format(getMessage(), args);
    }
}
