package com.dangbun.domain.user.adapter.in.web.dto.response;

public record KakaoAuthResponse(
        String code,
        String error,
        String error_description,
        String state
) {
}
