package com.dangbun.domain.place.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidInformationException extends RuntimeException{

    private final ResponseStatus exceptionStatus;

    public InvalidInformationException(ResponseStatus status){
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
