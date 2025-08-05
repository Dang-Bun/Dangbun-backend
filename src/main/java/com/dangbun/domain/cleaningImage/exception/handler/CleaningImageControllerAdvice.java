package com.dangbun.domain.cleaningImage.exception.handler;

import com.dangbun.domain.cleaningImage.exception.custom.InvalidS3KeyException;
import com.dangbun.domain.cleaningImage.exception.custom.NoSuchImageException;
import com.dangbun.global.response.BaseErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CleaningImageControllerAdvice {
    @ExceptionHandler(NoSuchImageException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(NoSuchImageException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }

    @ExceptionHandler(InvalidS3KeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseErrorResponse handleInvalidRoleException(InvalidS3KeyException e) {
        return new BaseErrorResponse(e.getExceptionStatus());
    }
}
