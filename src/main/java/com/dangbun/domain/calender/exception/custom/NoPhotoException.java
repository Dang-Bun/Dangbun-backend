package com.dangbun.domain.calender.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NoPhotoException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public NoPhotoException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }}
