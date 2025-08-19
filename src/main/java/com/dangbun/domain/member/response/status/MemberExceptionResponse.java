package com.dangbun.domain.member.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MemberExceptionResponse implements ResponseStatus {
    INVALID_ROLE(70000,"해당하는 역할은 요청을 수행할 수 없습니다."),
    MEMBER_NOT_FOUND(70001, "해당하는 맴버가 없습니다."),
    PLACE_ACCESS_DENIED(70002,"해당 플레이스에 소속된 맴버가 아닙니다."),
    NAME_NOT_MATCHED(70003, "요청한 맴버와 이름이 일치하지 않습니다."),
    PLACE_NAME_NOT_MATCHED(70004, "나가기 위한 플레이스 이름과 일치하지 않습니다."),
    MEMBERSHIP_UNAUTHORIZED(70005, "로그인되어 있지 않거나 멤버십 인증을 거치지 않았습니다."),
    DUTY_NOT_IN_PLACE(70006, "해당 플레이스에 존재하지 않는 당번입니다."),
    MEMBER_DUTY_ALREADY_ASSIGNED(70007, "이미 해당 당번에 지정되어있는 멤버입니다."),
    ;

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
