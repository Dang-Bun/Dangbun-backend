package com.dangbun.domain.notificationreceiver.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NotificationReceiverNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public NotificationReceiverNotFoundException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

}