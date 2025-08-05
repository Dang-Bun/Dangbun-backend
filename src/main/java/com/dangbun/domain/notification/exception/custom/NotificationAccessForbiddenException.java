package com.dangbun.domain.notification.exception.custom;


import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class NotificationAccessForbiddenException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public NotificationAccessForbiddenException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }

}