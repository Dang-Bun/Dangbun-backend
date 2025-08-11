package com.dangbun.domain.place.exception.handler;

import com.dangbun.domain.place.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.*;

@RestControllerAdvice
public class PlaceControllerAdvice {

    @ExceptionHandler(InvalidInviteCodeException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseErrorResponse handleInvalidRoleException(InvalidInviteCodeException e) {
        return new BaseErrorResponse(INVALID_INVITE_CODE);
    }

    @ExceptionHandler(AlreadyInvitedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleAlreadyInvitedException(AlreadyInvitedException e) {
        return new BaseErrorResponse(ALREADY_INVITED);
    }

    @ExceptionHandler(InvalidInformationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidInformationException(InvalidInformationException e) {
        return new BaseErrorResponse(INVALID_INFORMATION);
    }

    @ExceptionHandler(InvalidTimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidTimeException(InvalidTimeException e) {
        return new BaseErrorResponse(INVALID_TIME);
    }

    @ExceptionHandler(InvalidPlaceNameException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidPlaceNameException(InvalidPlaceNameException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
