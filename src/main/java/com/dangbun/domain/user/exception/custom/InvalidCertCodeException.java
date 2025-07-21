package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;

public class InvalidCertCodeException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InvalidCertCodeException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
