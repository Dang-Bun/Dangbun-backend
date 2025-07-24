package com.dangbun.domain.place.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaceExceptionResponse implements ResponseStatus {

    NO_SUCH_INVITE_CODE(80000,"참여코드에 해당하는 플레이스가 없습니다.");

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
