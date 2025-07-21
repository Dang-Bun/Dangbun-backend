package com.dangbun.global.response.status;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum BaseExceptionResponse implements ResponseStatus {
    SUCCESS(20000, "요청에 성공했습니다."),
    BAD_REQUEST(40000, "유효하지 않은 요청입니다."),
    NOT_FOUND(40400, "존재하지 않는 API입니다."),
    INTERNAL_SERVER_ERROR(50000, "서버 내부 오류입니다."),

    REQUIRED_FIELD_MISSING(10000, "필수 입력값이 누락되었습니다.")
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
