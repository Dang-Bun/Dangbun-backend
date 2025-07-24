package com.dangbun.domain.place.exception.handler;

import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PlaceControllerAdvice {

    @ExceptionHandler(InvalidInviteCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleInvalidRoleException(InvalidInviteCodeException e) {
        return new BaseErrorResponse(e.getStatus());
    }
}
