package com.dangbun.domain.duty.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum DutyExceptionResponse implements ResponseStatus {
    PLACE_NOT_FOUND(90000, "해당 장소가 존재하지 않습니다."),
    DUTY_ALREADY_EXISTS(90001, "이미 존재하는 당번입니다."),
    DUTY_NOT_FOUND(90002, "해당 당번이 존재하지 않습니다."),
    MEMBER_NOT_FOUND(90003, "해당 멤버가 존재하지 않습니다."),
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
