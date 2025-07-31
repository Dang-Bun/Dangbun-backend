package com.dangbun.domain.notification.exception.handler;

import com.dangbun.domain.notification.exception.custom.PlaceNotFoundException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.notification.response.status.NotificationExceptionResponse.*;

@RestControllerAdvice(basePackages = "com.dangbun.domain.notification")
public class NotificationControllerAdvice {

    @ExceptionHandler(PlaceNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handlePlaceNotFoundException(PlaceNotFoundException e) {
        return new BaseErrorResponse(PLACE_NOT_FOUND);
    }
}


