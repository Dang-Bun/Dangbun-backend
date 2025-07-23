package com.dangbun.domain.user.exception;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ResponseStatus {

    EXIST_EMAIL(20001, "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(20002, "유효하지 않은 패스워드입니다."),
    INVALID_CERT_CODE(20003, "유효하지 않은 인증번호 입니다."),
    NO_SUCH_USER(20004, "해당 유저가 존재하지 않습니다."),
    INVALID_EMAIL(20005, "유효하지 않은 이메일입니다.");
    private final int code;
    private final String message;
}
