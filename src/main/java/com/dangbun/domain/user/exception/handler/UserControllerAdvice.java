package com.dangbun.domain.user.exception.handler;

import com.dangbun.domain.user.exception.ErrorCode;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.global.response.BaseErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.dangbun.domain.user.exception.ErrorCode.*;

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

}
