package com.dangbun.domain.user;

public record KakaoAuthResponse(
        String code,
        String error,
        String error_description,
        String state
) {
}
