package com.dangbun.domain.notification.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationExceptionResponse implements ResponseStatus {
    PLACE_NOT_FOUND(120000, "해당 플레이스가 존재하지 않습니다."),

    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
