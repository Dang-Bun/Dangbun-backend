package com.dangbun.domain.cleaningImage.response.status;

import com.dangbun.global.response.status.ResponseStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum CleaningImageExceptionResponse implements ResponseStatus {
    NO_SUCH_IMAGE(140000,"요청한 이미지가 존재하지 않습니다."),
    INVALID_S3_KEY(140001, "유효하지 않은 S3 Key 입니다");

    private final int code;
    private final String message;

    @Override
    public int getCode() { return code; }

    @Override
    public String getMessage() { return message; }
}