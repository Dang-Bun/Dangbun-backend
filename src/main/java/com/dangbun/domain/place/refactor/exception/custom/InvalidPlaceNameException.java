package com.dangbun.domain.place.refactor.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidPlaceNameException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public InvalidPlaceNameException(ResponseStatus status){
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
