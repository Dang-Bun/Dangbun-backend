package com.dangbun.domain.notification.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NotificationNotFoundException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public NotificationNotFoundException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

}