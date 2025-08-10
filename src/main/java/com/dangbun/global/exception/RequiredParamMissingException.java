package com.dangbun.global.exception;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class RequiredParamMissingException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public RequiredParamMissingException(ResponseStatus exceptionStatus) {
        super(exceptionStatus.getMessage());
        this.exceptionStatus = exceptionStatus;
    }
}
