package com.dangbun.global.exception.handler;


import com.dangbun.global.exception.InvalidJwtExcepetion;
import com.dangbun.global.exception.InvalidRefreshJWTException;
import com.dangbun.global.exception.RequiredParamMissingException;
import com.dangbun.global.exception.UnautheniticatedException;
import com.dangbun.global.response.BaseErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.hibernate.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Order
@Slf4j
@RestControllerAdvice
public class GlobalControllerAdvice {

    // 잘못된 요청일 경우
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({BadRequestException.class, TypeMismatchException.class, MissingServletRequestParameterException.class})
    public BaseErrorResponse handle_BadRequest(Exception e){
        log.error("[handle_BadRequest]", e);
        return new BaseErrorResponse(BAD_REQUEST);
    }

    // 요청한 api가 없을 경우
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public BaseErrorResponse handle_NoHandlerFoundException(Exception e){
        log.error("[handle_NoHandlerFoundException]", e);
        return new BaseErrorResponse(NOT_FOUND);
    }

    // 런타임 오류가 발생한 경우
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public BaseErrorResponse handle_RuntimeException(Exception e) {
        log.error("[handle_RuntimeException]", e);
        return new BaseErrorResponse(INTERNAL_SERVER_ERROR);
    }

    // DTO validation 실패 (예: @NotBlank, @NotNull 등)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseErrorResponse handleValidationException(Exception e) {
        log.error("[handle_ValidationException]", e);
        return new BaseErrorResponse(REQUIRED_FIELD_MISSING);
    }

    // 필수 파라미터 누락
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ConstraintViolationException.class, RequiredParamMissingException.class})
    public BaseErrorResponse handleRequiredParamMissingExcpetion(Exception e) {
        log.warn("[handle_RequiredParamMissing]", e);
        return new BaseErrorResponse(REQUIRED_PARAM_MISSING);
    }

    // 인증되지 않은 사용자
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnautheniticatedException.class)
    public BaseErrorResponse handleUnauthenticated(Exception e) {
        log.warn("[handle_Unauthenticated]", e);
        return new BaseErrorResponse(AUTH_UNAUTHENTICATED);
    }

    // JWT 유효하지 않음
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidJwtExcepetion.class)
    public BaseErrorResponse handleInvalidJWT(Exception e) {
        log.warn("[handle_InvalidJWT]", e);
        return new BaseErrorResponse(INVALID_JWT);
    }

    // Refresh Token 유효하지 않음
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidRefreshJWTException.class)
    public BaseErrorResponse handleInvalidRefreshJWT(Exception e) {
        log.warn("[handle_InvalidRefreshJWT]", e);
        return new BaseErrorResponse(INVALID_REFRESH_TOKEN);
    }

}
