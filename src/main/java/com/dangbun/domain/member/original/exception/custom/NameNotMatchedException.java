package com.dangbun.domain.member.original.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NameNotMatchedException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public NameNotMatchedException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
