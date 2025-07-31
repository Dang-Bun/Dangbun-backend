package com.dangbun.global.exception.handler;


import com.dangbun.global.exception.BadRequestException;
import com.dangbun.global.response.BaseErrorResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Order(Ordered.LOWEST_PRECEDENCE)
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

    // RequestParam, PathVariable 등의 validation 실패 (예: @RequestParam 제약 위반)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public BaseErrorResponse handleConstraintViolation(Exception e) {
        return new BaseErrorResponse(REQUIRED_FIELD_MISSING);
    }

    // 런타임 시 표현식 구문 오류, 변수 또는 속성 접근 실패 시 발생
    // (@AuthenticationPrincipal(expression="user")에서 토큰 만료 시 발생 가능
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(SpelEvaluationException.class)
    public void handleSpelEvaluation(Exception e){
        log.error("[handle_SpelEvaluationException]",e);
    }
}
