package com.dangbun.domain.user.dto.response;

import com.dangbun.domain.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetUserMyInfoResponse (
        @Schema(description = "사용자 이름", example = "전혜영")
        String name,

        @Schema(description = "사용자 이메일", example = "Kuit1234@naver.com")
        String email
) {
    public static GetUserMyInfoResponse from(User user) {
        return new GetUserMyInfoResponse(user.getName(), user.getEmail());
    }
}
