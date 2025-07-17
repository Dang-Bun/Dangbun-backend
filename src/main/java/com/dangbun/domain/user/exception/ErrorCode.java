package com.dangbun.domain.user.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXIST_EMAIL(20001, "이미 존재하는 이메일입니다.");

    private final int code;
    private final String message;
}
