package com.dangbun.domain.user.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;

public class InvalidPasswordException extends RuntimeException{

    private final ResponseStatus exceptionStatus;


    public InvalidPasswordException(ResponseStatus status){
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
