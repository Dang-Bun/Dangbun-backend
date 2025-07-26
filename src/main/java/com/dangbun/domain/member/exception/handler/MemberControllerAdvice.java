package com.dangbun.domain.member.exception.handler;

import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.exception.custom.MemberNotFoundException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class MemberControllerAdvice {

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(InvalidRoleException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
