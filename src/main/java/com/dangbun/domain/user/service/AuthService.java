package com.dangbun.domain.user.service;


import com.dangbun.domain.user.dto.request.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.DeleteMemberException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.security.JwtUtil;
import com.dangbun.global.security.TokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final JwtUtil jwtUtil;
    private final RedisTemplate redisTemplate;

    @Transactional(readOnly = true)
    public PostUserLoginResponse login(@Valid PostUserLoginRequest request) {


        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (!user.getEnabled()) {
            throw new DeleteMemberException(DELETE_MEMBER);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        final String accessToken = tokenProvider.createAccessToken(user);
        final String refreshToken = tokenProvider.createRefreshToken(user);

        tokenProvider.saveRefreshToken(user.getUserId(), refreshToken);

        return new PostUserLoginResponse(accessToken, refreshToken);
    }


    public void logout(String bearerToken) {
        String accessToken = jwtUtil.parseAccessToken(bearerToken);
        String userId = tokenProvider.validateAndGetUserId(accessToken);

        redisTemplate.delete("refreshToken:" + userId);

        Date expiration = jwtUtil.getExpiration(accessToken);
        Long now = System.currentTimeMillis();
        Long expirationMs = expiration.getTime() - now;

        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout", expirationMs, TimeUnit.MILLISECONDS);

    }

}
