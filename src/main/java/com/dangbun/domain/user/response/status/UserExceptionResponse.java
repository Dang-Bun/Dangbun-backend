package com.dangbun.domain.user.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserExceptionResponse implements ResponseStatus {
    USER_NOT_FOUND(60000, "존재하지 않는 사용자입니다.")
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}