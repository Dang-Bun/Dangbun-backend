package com.dangbun.global.redis;

import com.dangbun.domain.user.exception.custom.AuthCodeAlreadySentException;
import com.dangbun.global.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class AuthRedisService {

    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public final static Long REFRESH_VALIDATION_MS = 1000L * 60 * 60 * 24 * 15;


    public void deleteAndSetBlacklist(String bearerToken){
        String accessToken = jwtUtil.parseAccessToken(bearerToken);
        String userId = jwtUtil.validateAndGetUserId(accessToken);



        redisTemplate.delete("refreshToken:" + userId);

        Date expiration = jwtUtil.getExpiration(accessToken);
        Long now = System.currentTimeMillis();
        Long expirationMs = expiration.getTime() - now;

        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout", expirationMs, TimeUnit.MILLISECONDS);
    }

    public void saveRefreshToken(Long userId, String refreshToken) {
        redisTemplate.opsForValue()
                .set("refreshToken:" + userId, refreshToken, Duration.ofMillis(REFRESH_VALIDATION_MS));
    }

    public void saveAuthCode(String toEmail, String authCode, Duration duration){


        redisTemplate.opsForValue()
                .set(toEmail, authCode, duration);
    }

    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get(email);
    }

    public void checkDuration(String toEmail, Duration duration) {
        long remaining = redisTemplate.getExpire(toEmail, TimeUnit.SECONDS);

        if(remaining > 0 && remaining > duration.getSeconds() - 60){
            throw new AuthCodeAlreadySentException(AUTH_CODE_SENT);
        }
    }
}

