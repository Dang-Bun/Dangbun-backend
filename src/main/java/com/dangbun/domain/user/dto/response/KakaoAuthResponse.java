package com.dangbun.domain.user.dto.response;

public record KakaoAuthResponse(
        String code,
        String error,
        String error_description,
        String state
) {
}
