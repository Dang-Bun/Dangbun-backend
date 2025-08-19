package com.dangbun.domain.user.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserExceptionResponse implements ResponseStatus {
    EXIST_EMAIL(60000, "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(60001, "유효하지 않은 패스워드입니다."),
    INVALID_CERT_CODE(60002, "유효하지 않은 인증번호 입니다."),
    NO_SUCH_USER(60003, "해당 유저가 존재하지 않습니다."),
    INVALID_EMAIL(60004, "유효하지 않은 이메일입니다."),
    DELETE_MEMBER(60005, "탈퇴한 회원입니다."),
    AUTH_CODE_SENT(60006,"인증코드가 이미 전송되었습니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}