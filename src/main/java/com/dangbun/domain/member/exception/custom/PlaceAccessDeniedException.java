package com.dangbun.domain.member.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class PlaceAccessDeniedException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public PlaceAccessDeniedException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
