package com.dangbun.global.exception;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidJwtExcepetion extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InvalidJwtExcepetion(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
