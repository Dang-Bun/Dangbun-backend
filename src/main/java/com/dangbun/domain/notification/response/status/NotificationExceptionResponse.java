package com.dangbun.domain.notification.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationExceptionResponse implements ResponseStatus {
    MEMBER_NOT_FOUND(120000, "해당 멤버가 존재하지 않습니다."),
    NOTIFICATION_NOT_FOUND(120001, "해당 알림이 존재하지 않습니다."),
    NOTIFICATION_ACCESS_FORBIDDEN(120002, "해당 알림에 접근할 수 없습니다.");

    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
