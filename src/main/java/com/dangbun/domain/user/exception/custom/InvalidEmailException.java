package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;

public class InvalidEmailException extends RuntimeException{

    private final ResponseStatus exceptionStatus;

    public InvalidEmailException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
