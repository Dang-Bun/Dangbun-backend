package com.dangbun.domain.user.adapter.out.external;

import com.dangbun.domain.user.application.port.out.TokenBlacklistPort;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBlacklistAdapter implements TokenBlacklistPort {

    private final AuthRedisService authRedisService;
    private final StringRedisTemplate redisTemplate;

    @Override
    public void blacklistToken(String token) {
        authRedisService.deleteAndSetBlacklist(token);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String accessToken = JwtUtil.parseAccessToken(token);
        return redisTemplate.hasKey("blacklist:" + accessToken);
    }
}
