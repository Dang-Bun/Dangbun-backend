package com.dangbun.domain.user.exception.handler;

import com.dangbun.domain.user.exception.custom.*;
import com.dangbun.global.response.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
@RestControllerAdvice(basePackages = "com.dangbun.domain.user")
public class UserControllerAdvice {

    @ExceptionHandler(InvalidPasswordException.class)
    public BaseErrorResponse handleInvalidPassword(InvalidPasswordException e){
        log.error("[InvalidPasswordException]",e);
        return new BaseErrorResponse(INVALID_PASSWORD);
    }

    @ExceptionHandler(ExistEmailException.class)
    public BaseErrorResponse handleExistEmail(ExistEmailException e){
        log.error("[ExistEmailException]",e);
        return new BaseErrorResponse(EXIST_EMAIL);
    }

    @ExceptionHandler(InvalidCertCodeException.class)
    public BaseErrorResponse handleInvalidCertCode(InvalidCertCodeException e){
        log.error("[InvalidCertCodeException]",e);
        return new BaseErrorResponse(INVALID_CERT_CODE);
    }

    @ExceptionHandler(InvalidEmailException.class)
    public BaseErrorResponse handleInvalidEmail(InvalidEmailException e){
        log.error("[InvalidEmailException]",e);
        return new BaseErrorResponse(INVALID_EMAIL);
    }

    @ExceptionHandler(NoSuchUserException.class)
    public BaseErrorResponse handleUserNotFound(NoSuchUserException e){
        log.error("[UserNotFoundException]",e);
        return new BaseErrorResponse(NO_SUCH_USER);
    }

    @ExceptionHandler(DeleteMemberException.class)
    public BaseErrorResponse handleUserNotFound(DeleteMemberException e){
        log.error("[DeleteMemberException]",e);
        return new BaseErrorResponse(DELETE_MEMBER);
    }



}
