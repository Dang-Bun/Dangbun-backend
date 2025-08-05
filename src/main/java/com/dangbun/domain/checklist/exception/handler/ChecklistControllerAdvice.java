package com.dangbun.domain.checklist.exception.handler;

import com.dangbun.domain.checklist.exception.custom.ChecklistAccessDeniedException;
import com.dangbun.domain.checklist.exception.custom.ChecklistStatusConflictException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChecklistControllerAdvice {

    @ExceptionHandler(ChecklistAccessDeniedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(ChecklistAccessDeniedException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(ChecklistStatusConflictException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(ChecklistStatusConflictException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
