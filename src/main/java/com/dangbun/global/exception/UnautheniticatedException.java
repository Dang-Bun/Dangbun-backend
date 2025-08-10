package com.dangbun.global.exception;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;


@Getter
public class UnautheniticatedException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public UnautheniticatedException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
