package com.dangbun.domain.member.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberExceptionResponse implements ResponseStatus {
    INVALID_ROLE(70000,"해당하는 역할은 요청을 수행할 수 없습니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
