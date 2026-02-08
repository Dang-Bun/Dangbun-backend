package com.dangbun.domain.calendar.refactor.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidDateException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public InvalidDateException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
