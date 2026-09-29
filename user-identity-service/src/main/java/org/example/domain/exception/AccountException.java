package org.example.domain.exception;

import org.example.exception.BaseException;
import org.example.response.BaseErrorCode;

public class AccountException extends BaseException {

    public AccountException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
