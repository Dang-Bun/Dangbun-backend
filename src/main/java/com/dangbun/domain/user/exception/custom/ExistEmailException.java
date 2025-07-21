package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;

public class ExistEmailException extends RuntimeException {

    private final ResponseStatus exceptionStatus;


    public ExistEmailException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
