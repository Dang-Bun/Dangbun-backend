package com.dangbun.domain.member.exception.handler;

import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class MemberControllerAdvice {

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseErrorResponse handleInvalidRoleException(InvalidRoleException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(PlaceAccessDeniedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handlePlaceAccessDeniedException(PlaceAccessDeniedException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(NameNotMatchedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleNameNotMatched(NameNotMatchedException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(PlaceMembershipUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseErrorResponse handleNameNotMatched(PlaceMembershipUnauthorizedException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
