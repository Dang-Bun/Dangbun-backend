package com.dangbun.domain.user.domain;

/**
 * 인증 토큰 Value Object
 */
public record AuthToken(
        String accessToken,
        String refreshToken
) {
    public static AuthToken of(String accessToken, String refreshToken) {
        return new AuthToken(accessToken, refreshToken);
    }
}
