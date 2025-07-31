package com.dangbun.domain.notification.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class PlaceNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public PlaceNotFoundException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}