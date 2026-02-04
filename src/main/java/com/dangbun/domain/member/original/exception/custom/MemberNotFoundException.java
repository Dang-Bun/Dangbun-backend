package com.dangbun.domain.member.original.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public MemberNotFoundException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
