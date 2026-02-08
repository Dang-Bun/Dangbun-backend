package com.dangbun.domain.cleaningImage.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class CleaningImageAlreadyExistsException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public CleaningImageAlreadyExistsException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
