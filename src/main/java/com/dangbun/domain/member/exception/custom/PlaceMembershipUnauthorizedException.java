package com.dangbun.domain.member.exception.custom;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;

@Getter
public class PlaceMembershipUnauthorizedException extends RuntimeException {
    private final ResponseStatus exceptionStatus;

    public PlaceMembershipUnauthorizedException(ResponseStatus status) {
        super(status.getMessage());
        this.exceptionStatus = status;
    }
}