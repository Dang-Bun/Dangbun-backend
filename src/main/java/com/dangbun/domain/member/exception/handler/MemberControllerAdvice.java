package com.dangbun.domain.member.exception.handler;

import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.*;


@RestControllerAdvice
public class MemberControllerAdvice {

    @ExceptionHandler(InvalidRoleException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseErrorResponse handleInvalidRoleException(InvalidRoleException e) {
        return new BaseErrorResponse(INVALID_ROLE);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleMemberNotFoundException(MemberNotFoundException e) {
        return new BaseErrorResponse(MEMBER_NOT_FOUND);
    }

    @ExceptionHandler(PlaceAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseErrorResponse handlePlaceAccessDeniedException(PlaceAccessDeniedException e) {
        return new BaseErrorResponse(PLACE_ACCESS_DENIED);
    }

    @ExceptionHandler(NameNotMatchedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleNameNotMatched(NameNotMatchedException e) {
        return new BaseErrorResponse(NAME_NOT_MATCHED);
    }

    @ExceptionHandler(PlaceNameNotMatchedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleNameNotMatched(PlaceNameNotMatchedException e) {
        return new BaseErrorResponse(PLACE_NAME_NOT_MATCHED);
    }

    @ExceptionHandler(MembershipUnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public BaseErrorResponse handleNameNotMatched(MembershipUnauthorizedException e) {
        return new BaseErrorResponse(MEMBERSHIP_UNAUTHORIZED);
    }

}
