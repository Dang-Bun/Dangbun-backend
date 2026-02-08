package com.dangbun.domain.user.adapter.out.external.jwt;

import com.dangbun.common.hexagonal.ExternalAdapter;
import com.dangbun.domain.user.application.port.out.TokenPort;
import com.dangbun.domain.user.domain.AuthToken;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.jwt.JwtProvider;
import com.dangbun.global.security.jwt.JwtUtil;
import com.dangbun.domain.user.adapter.out.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;

@ExternalAdapter
@RequiredArgsConstructor
public class JwtTokenAdapter implements TokenPort {

    private final JwtProvider jwtProvider;
    private final AuthRedisService authRedisService;

    @Override
    public AuthToken generateTokens(Long userId, String email) {
        String accessToken = jwtProvider.createAccessToken(email);
        // JwtProvider.createRefreshToken은 User 엔티티가 필요하지만,
        // 이메일만 사용하므로 내부적으로 빌드
        UserJpaEntity tempUserJpaEntity = UserJpaEntity.builder().email(email).build();
        String refreshToken = jwtProvider.createRefreshToken(tempUserJpaEntity);

        return AuthToken.of(accessToken, refreshToken);
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return JwtUtil.validateToken(token);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String getEmailFromToken(String token) {
        return JwtUtil.getSubject(JwtUtil.stripBearer(token));
    }

    @Override
    public Long getUserIdFromToken(String token) {
        // 현재 JWT에 userId를 저장하지 않으므로 null 반환
        // 필요시 JWT claim에 userId 추가 후 구현
        return null;
    }

    @Override
    public void saveRefreshToken(Long userId, String refreshToken) {
        authRedisService.saveRefreshToken(userId, refreshToken);
    }
}
