package com.dangbun.domain.place.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidInviteCodeException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InvalidInviteCodeException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
