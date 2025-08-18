package com.dangbun.domain.notificationreceiver.exception.handler;

import com.dangbun.domain.notification.exception.custom.MemberNotFoundException;
import com.dangbun.domain.notificationreceiver.exception.custom.NotificationReceiverNotFoundException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import static com.dangbun.domain.notificationreceiver.response.status.NotificationReceiverExceptionResponse.NOTIFICATION_RECEIVER_NOT_FOUND;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.dangbun.domain.notificationreceiver")
public class NotificationReceiverControllerAdvice {

    @ExceptionHandler(NotificationReceiverNotFoundException.class)
    public BaseErrorResponse handleMemberNotFoundException(NotificationReceiverNotFoundException e) {
        return new BaseErrorResponse(NOTIFICATION_RECEIVER_NOT_FOUND);
    }


}