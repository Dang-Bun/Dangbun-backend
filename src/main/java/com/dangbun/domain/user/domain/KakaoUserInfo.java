package com.dangbun.domain.user.domain;

/**
 * 카카오 사용자 정보 Value Object
 */
public record KakaoUserInfo(
        String socialId,
        String email,
        String nickname
) {
    public static KakaoUserInfo of(String socialId, String email, String nickname) {
        return new KakaoUserInfo(socialId, email, nickname);
    }
}
