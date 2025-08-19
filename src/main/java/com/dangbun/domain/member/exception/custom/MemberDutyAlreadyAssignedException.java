package com.dangbun.domain.member.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class MemberDutyAlreadyAssignedException extends RuntimeException {

    private final ResponseStatus exceptionStatus;

    public MemberDutyAlreadyAssignedException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}