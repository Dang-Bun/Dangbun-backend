package com.dangbun.domain.user.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserExceptionResponse implements ResponseStatus {
    USER_NOT_FOUND(60000, "존재하지 않는 사용자입니다."),
    EXIST_EMAIL(60001, "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(60002, "유효하지 않은 패스워드입니다."),
    INVALID_CERT_CODE(60003, "유효하지 않은 인증번호 입니다."),
    NO_SUCH_USER(60004, "해당 유저가 존재하지 않습니다."),
    INVALID_EMAIL(60005, "유효하지 않은 이메일입니다."),
    DELETE_MEMBER(60006, "탈퇴한 회원입니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}