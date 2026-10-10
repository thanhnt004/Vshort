package org.example.response;

public enum CommonErrorCode implements BaseErrorCode{

    // Lỗi 500: Lỗi hệ thống không lường trước được
    UNCATEGORIZED_EXCEPTION("UNCATEGORIZED_EXCEPTION", "Uncategorized error", 500),
    BAD_REQUEST("BAD_REQUEST", "Bad request", 400);
    CommonErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final String code;
    private final String message;
    private final int statusCode;
    @Override
    public String getCode() {
        return "";
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public int getStatusCode() {
        return 0;
    }
}
