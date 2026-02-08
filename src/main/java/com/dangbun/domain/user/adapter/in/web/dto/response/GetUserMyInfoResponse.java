package com.dangbun.domain.user.adapter.in.web.dto.response;

import com.dangbun.domain.user.adapter.out.persistence.UserJpaEntity;
import com.dangbun.domain.user.application.port.in.query.UserInfoResult;
import com.dangbun.domain.user.domain.User;
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

    public static GetUserMyInfoResponse from(UserInfoResult result) {
        return new GetUserMyInfoResponse(result.name(), result.email());
    }

    // 기존 JPA 엔티티 호환용
    public static GetUserMyInfoResponse from(UserJpaEntity userJpaEntity) {
        return new GetUserMyInfoResponse(userJpaEntity.getName(), userJpaEntity.getEmail());
    }
}
