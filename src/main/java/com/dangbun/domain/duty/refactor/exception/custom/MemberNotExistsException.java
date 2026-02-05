package com.dangbun.domain.duty.refactor.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class MemberNotExistsException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public MemberNotExistsException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
