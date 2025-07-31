package com.dangbun.domain.place.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidTimeException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InvalidTimeException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }}
