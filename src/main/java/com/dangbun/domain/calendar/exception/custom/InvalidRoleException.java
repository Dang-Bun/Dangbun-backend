package com.dangbun.domain.calendar.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidRoleException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public InvalidRoleException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
