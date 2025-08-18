package com.dangbun.domain.calendar.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CalendarExceptionResponse implements ResponseStatus {
    FUTURE_DATE_NOT_ALLOWED(150000,"오늘 및 과거의 날짜만 선택할 수 있습니다."),
    INVALID_ROLE(150001, "체크리스트를 조작할 권한이 없습니다."),
    NO_PHOTO(150002,"사진이 없는 체크리스트 입니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
