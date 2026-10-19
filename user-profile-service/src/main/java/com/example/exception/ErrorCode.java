package com.example.exception;

import org.example.response.BaseErrorCode;

public enum ErrorCode implements BaseErrorCode {
    PROFILE_NOT_FOUND("PROFILE_NOT_FOUND","Profile of this account not found!",400),

    ;
    private final String code;
    private final String message;
    private final int statusCode;

    ErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }
}
