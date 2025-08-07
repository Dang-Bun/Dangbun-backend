package com.dangbun.domain.calender.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CalenderExceptionResponse implements ResponseStatus {
    FUTURE_DATE_NOT_ALLOWED(140000,"오늘 및 과거의 날짜만 선택할 수 있습니다."),
    INVALID_ROLE(140001, "체크리스트를 조작할 권한이 없습니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
