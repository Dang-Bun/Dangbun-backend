package com.dangbun.domain.place.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InviteCodeNotExistsException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InviteCodeNotExistsException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}