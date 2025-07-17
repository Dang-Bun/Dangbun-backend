package com.dangbun.global.response;

import com.dangbun.global.response.status.ResponseStatus;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"code", "message", "data"})
public class BaseErrorResponse implements ResponseStatus {
    private final int code;
    private final String message;
    private final Object data;

    public BaseErrorResponse(ResponseStatus status) {
        this.code = status.getCode();
        this.message = status.getMessage();
        this.data = null;
    }

    public BaseErrorResponse(ResponseStatus status, String message) {
        this.code = status.getCode();
        this.message = message;
        this.data = null;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
