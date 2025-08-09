package com.dangbun.domain.calender.exception.handler;

import com.dangbun.domain.calender.exception.custom.InvalidDateException;
import com.dangbun.domain.calender.exception.custom.InvalidRoleException;
import com.dangbun.domain.calender.exception.custom.NoPhotoException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CalenderControllerAdvice {

    @ExceptionHandler(InvalidDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidDateException(InvalidDateException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(InvalidRoleException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(NoPhotoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleNoPhotoException(NoPhotoException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

}
