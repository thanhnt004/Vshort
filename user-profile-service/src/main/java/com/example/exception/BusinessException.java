package com.example.exception;

import org.example.exception.BaseException;
import org.example.response.BaseErrorCode;

public class BusinessException extends BaseException {
    public BusinessException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
