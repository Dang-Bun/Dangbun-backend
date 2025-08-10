package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NoSuchUserException extends RuntimeException{
    private final ResponseStatus exceptionStatus;

    public NoSuchUserException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}