package com.dangbun.domain.cleaningImage.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class InvalidS3KeyException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public InvalidS3KeyException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
