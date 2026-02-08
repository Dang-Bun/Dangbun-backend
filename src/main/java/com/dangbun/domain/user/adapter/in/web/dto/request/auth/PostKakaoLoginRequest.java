package com.dangbun.domain.user.adapter.in.web.dto.request.auth;

public record PostKakaoLoginRequest(
        String code,
        String error,
        String error_description,
        String state

)implements LoginRequest {
    public static PostKakaoLoginRequest of(String code, String error, String error_description, String state){
        return new PostKakaoLoginRequest(code, error, error_description, state);
    }
}
