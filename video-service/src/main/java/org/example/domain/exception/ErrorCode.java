package org.example.domain.exception;

import org.example.response.BaseErrorCode;

public enum ErrorCode implements BaseErrorCode {
    INVALID_FILE_SIZE("INVALID_FILE_SIZE", "Dung lượng tệp không hợp lệ.", 400),
    FILE_SIZE_EXCEEDED("FILE_SIZE_EXCEEDED", "Dung lượng tệp vượt quá giới hạn cho phép.", 400),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "Loại tệp không hợp lệ.", 400),
    INVALID_FILE_EXTENSION("INVALID_FILE_EXTENSION", "Phần mở rộng tệp không hợp lệ.", 400),

    UPLOAD_SESSION_NOT_FOUND("UPLOAD_SESSION_NOT_FOUND", "Phiên upload không tồn tại.", 400),
    BATCH_INVALID("BATCH_INVALID", "Số phần không hợp lệ.", 400),
    DATA_INTEGRITY_CHECK_FAILED("DATA_INTEGRITY_CHECK_FAILED", "Kiểm tra tính toàn vẹn dữ liệu thất bại.", 400),
    USER_NOT_OWNER("USER_NOT_OWNER", "Người dùng không có quyền sở hữu.", 400),
    UPLOAD_SESSION_COMPLETED("UPLOAD_SESSION_COMPLETED","Video đã tải lên hoàn tất! Không thể hủy" ,400 );
    private String code;
    private String message;
    private int statusCode;
    ErrorCode(String code, String message, int statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getStatusCode() {
        return statusCode;
    }
}
