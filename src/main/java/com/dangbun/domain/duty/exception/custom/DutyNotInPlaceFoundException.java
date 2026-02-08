package com.dangbun.domain.duty.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class DutyNotInPlaceFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public DutyNotInPlaceFoundException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
