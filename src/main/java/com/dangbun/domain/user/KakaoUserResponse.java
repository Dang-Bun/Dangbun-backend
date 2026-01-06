package com.dangbun.domain.user;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 카카오 사용자 정보 응답 Record
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record KakaoUserResponse(
        Long id,
        String connectedAt,
        KakaoAccount kakaoAccount
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record KakaoAccount(
            Boolean profileNicknameNeedsAgreement,
            Boolean profileImageNeedsAgreement,
            Profile profile,
            Boolean emailNeedsAgreement,
            Boolean isEmailValid,
            Boolean isEmailVerified,
            String email
    ) {
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Profile(
                String nickname,
                String thumbnailImageUrl,
                String profileImageUrl,
                Boolean isDefaultImage
        ) {}
    }

    public String getEmail() {
        return kakaoAccount != null ? kakaoAccount.email() : null;
    }

    public String getNickname() {
        if (kakaoAccount != null && kakaoAccount.profile() != null) {
            return kakaoAccount.profile().nickname();
        }
        return null;
    }
}