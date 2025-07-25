package com.dangbun.domain.cleaning.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CleaningExceptionResponse implements ResponseStatus {
    DUTY_NOT_FOUND(110000, "해당 당번이 존재하지 않습니다."),

    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}