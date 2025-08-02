package com.dangbun.domain.checklist.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class ChecklistAccessDeniedException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public ChecklistAccessDeniedException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}
