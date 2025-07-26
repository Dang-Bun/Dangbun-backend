package com.dangbun.domain.cleaning.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidDateFormatException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public InvalidDateFormatException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}