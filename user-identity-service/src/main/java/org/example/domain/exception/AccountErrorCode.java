package org.example.domain.exception;

import org.example.response.BaseErrorCode;

public enum AccountErrorCode implements BaseErrorCode {
    //Register
    USER_NAME_MISSING("USER_NAME_MISSING", "User name is required", 400),
    USER_NAME_EXISTS("USER_NAME_EXISTS", "User name already exists, please choose another one", 400),
    EMAIL_EXISTS("EMAIL_EXISTS", "Email already exists, please choose another one", 400),
    PHONE_EXISTS("PHONE_EXISTS", "Phone number already exists, please choose another one", 400),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", "Invalid email format", 400),
    INVALID_PHONE_FORMAT("INVALID_PHONE_FORMAT","Invalid phone format",400),
    // LOGIN
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", "Invalid username or password", 401),
    ACCOUNT_LOCKED("ACCOUNT_LOCKED", "Account is locked! Please try again later.", 403),
    ACCOUNT_VERIFY_NEED("ACCOUNT_VERIFY_NEED","You account's email is not verify yet! Please verify it.",400),
    ACCOUNT_DISABLED("ACCOUNT_DISABLED", "Account is disabled. Please contact support.", 403),
    UNAUTHORIZED_ACCESS("UNAUTHORIZED_ACCESS", "Authentication is required to access this resource", 401),
    ACCOUNT_NOT_FOUND("ACCOUNT_NOT_FOUND","Account is not exist!",400),
    //TOKEN
    INVALID_TOKEN("INVALID_TOKEN","Token is invalid",401),

    ;
    private final String code;
    private final String message;
    private final int statusCode;
    AccountErrorCode(String code, String message, int statusCode)
    {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    @Override
    public String getCode() { return code; }
    @Override
    public String getMessage() { return message; }
    @Override
    public int getStatusCode() { return statusCode; }
}
