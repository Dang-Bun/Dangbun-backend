package com.dangbun.domain.duty.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public MemberNotFoundException(ResponseStatus exceptionStatus, String message) {
        super(message);
        this.exceptionStatus = exceptionStatus;
    }

}