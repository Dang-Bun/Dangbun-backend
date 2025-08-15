package com.dangbun.domain.cleaning.exception.handler;

import com.dangbun.domain.cleaning.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(basePackages = "com.dangbun.domain.cleaning")
public class CleaningControllerAdvice {

    @ExceptionHandler(DutyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleDutyNotFoundException(DutyNotFoundException e) {
        return new BaseErrorResponse(DUTY_NOT_FOUND);
    }

    @ExceptionHandler(InvalidDateFormatException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleInvalidDateFormatException(InvalidDateFormatException e) {
        return new BaseErrorResponse(INVALID_DATE_FORMAT);
    }

    @ExceptionHandler(CleaningAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleCleaningAlreadyExistsException(CleaningAlreadyExistsException e) {
        return new BaseErrorResponse(CLEANING_ALREADY_EXISTS);
    }

    @ExceptionHandler(CleaningNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleCleaningNotFoundException(CleaningNotFoundException e) {
        return new BaseErrorResponse(CLEANING_NOT_FOUND);
    }


}
