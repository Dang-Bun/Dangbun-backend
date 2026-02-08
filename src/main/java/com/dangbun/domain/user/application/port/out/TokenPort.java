package com.dangbun.domain.user.application.port.out;

import com.dangbun.domain.user.domain.AuthToken;

/**
 * JWT 토큰 생성/검증을 위한 아웃고잉 포트
 */
public interface TokenPort {

    AuthToken generateTokens(Long userId, String email);

    boolean validateToken(String token);

    String getEmailFromToken(String token);

    Long getUserIdFromToken(String token);

    void saveRefreshToken(Long userId, String refreshToken);
}
