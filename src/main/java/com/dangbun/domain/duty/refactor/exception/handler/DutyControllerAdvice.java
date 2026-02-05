package com.dangbun.domain.duty.refactor.exception.handler;

import com.dangbun.domain.duty.refactor.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.duty.refactor.exception.status.DutyExceptionResponse.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.dangbun.domain.duty.refactor")
public class DutyControllerAdvice {

    @ExceptionHandler(DutyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleDutyAlreadyExistsException(DutyAlreadyExistsException e) {
        return new BaseErrorResponse(DUTY_ALREADY_EXISTS);
    }

    @ExceptionHandler(DutyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleDutyNotFoundException(DutyNotFoundException e) {
        return new BaseErrorResponse(DUTY_NOT_FOUND);
    }

    @ExceptionHandler(DutyNotInPlaceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleDutyNotInPlaceFoundException(DutyNotInPlaceFoundException e) {
        return new BaseErrorResponse(DUTY_NOT_IN_PLACE);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(MEMBER_NOT_FOUND);
    }

    @ExceptionHandler(CleaningNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleCleaningNotFoundException(CleaningNotFoundException e) {
        return new BaseErrorResponse(CLEANING_NOT_FOUND);
    }

    @ExceptionHandler(MemberNotExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleMemberNotExistsException(MemberNotExistsException e) {
        return new BaseErrorResponse(MEMBER_NOT_EXISTS);
    }

    @ExceptionHandler(CleaningNotAssignedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleCleaningNotAssignedException(CleaningNotAssignedException e) {
        return new BaseErrorResponse(CLEANING_NOT_ASSIGNED);
    }
}
