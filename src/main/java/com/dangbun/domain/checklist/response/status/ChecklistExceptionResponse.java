package com.dangbun.domain.checklist.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ChecklistExceptionResponse implements ResponseStatus {
    CHECKLIST_ACCESS_DENIED(130000,"해당 체크리스트를 조작할 권한이 없습니다."),

    ALREADY_CHECKED(130001, "이미 체크된 항목입니다."),
    ALREADY_UNCHECKED(130002, "이미 체크 해제된 항목입니다."),
    REQUIRE_IMAGE(130003, "체크 전 이미지 등록이 필요합니다.");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}
