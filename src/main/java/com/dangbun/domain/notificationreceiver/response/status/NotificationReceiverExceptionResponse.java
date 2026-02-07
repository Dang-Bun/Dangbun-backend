package com.dangbun.domain.notificationreceiver.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationReceiverExceptionResponse implements ResponseStatus {
    NOTIFICATION_RECEIVER_NOT_FOUND(160000, "알림 수신자를 찾을 수 없습니다.");
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}