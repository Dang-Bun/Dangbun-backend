package com.dangbun.domain.duty.original.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class CleaningNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public CleaningNotFoundException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}