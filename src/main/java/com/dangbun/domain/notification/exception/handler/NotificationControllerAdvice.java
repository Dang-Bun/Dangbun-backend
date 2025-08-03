package com.dangbun.domain.notification.exception.handler;

import com.dangbun.domain.notification.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.*;

@RestControllerAdvice(basePackages = "com.dangbun.domain.notification")
public class NotificationControllerAdvice {
    @ExceptionHandler(MemberNotFoundException.class)

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(MEMBER_NOT_FOUND);
    }
}


