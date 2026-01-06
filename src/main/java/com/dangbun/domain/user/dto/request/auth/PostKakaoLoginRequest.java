package com.dangbun.domain.user.dto.request.auth;

import jakarta.validation.constraints.NotEmpty;

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
