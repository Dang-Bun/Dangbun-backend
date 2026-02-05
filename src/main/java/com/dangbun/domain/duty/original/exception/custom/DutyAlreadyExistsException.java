package com.dangbun.domain.duty.original.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class DutyAlreadyExistsException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public DutyAlreadyExistsException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
