package com.dangbun.domain.duty.exception.handler;

import com.dangbun.domain.duty.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.duty.response.status.DutyExceptionResponse.*;

@RestControllerAdvice(basePackages = "com.dangbun.domain.duty")
public class DutyControllerAdvice {

    @ExceptionHandler(DutyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleDutyAlreadyExistsException(DutyAlreadyExistsException e) {
        return new BaseErrorResponse(DUTY_ALREADY_EXISTS);
    }

    @ExceptionHandler(PlaceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handlePlaceNotFoundException(PlaceNotFoundException e) {
        return new BaseErrorResponse(PLACE_NOT_FOUND);
    }

    @ExceptionHandler(DutyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleDutyNotFoundException(DutyNotFoundException e) {
        return new BaseErrorResponse(DUTY_NOT_FOUND);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(MEMBER_NOT_FOUND, e.getMessage());
    }
}
