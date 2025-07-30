package com.dangbun.domain.place.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaceExceptionResponse implements ResponseStatus {

    NO_SUCH_INVITE_CODE(80000,"참여코드에 해당하는 플레이스가 없습니다."),
    ALREADY_INVITED(80001, "이미 초대된 사용자입니다."),
    INVALID_INFORMATION(80002, "플레이스가 요구한 맴버정보와 요청이 일치하지 않습니다."),
    NO_SUCH_PLACE(80003, "해당하는 플레이스가 존재하지 않습니다."),
    INVALID_TIME(80004,"시작시간이 종료시간보다 늦게 설정되었습니다.");

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
