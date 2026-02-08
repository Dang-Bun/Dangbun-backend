package com.dangbun.global.security.jwt;

import com.dangbun.domain.user.adapter.out.persistence.UserJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.dangbun.global.security.jwt.TokenPrefix.*;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtProvider jwtProvider;


    public Map<String, String> generateToken(UserJpaEntity userJpaEntity) {

        Map<String , String> tokenMap = new HashMap<>();

        final String accessToken = jwtProvider.createAccessToken(userJpaEntity.getEmail());
        tokenMap.put(ACCESS.getName(),accessToken);

        final String refreshToken = jwtProvider.createRefreshToken(userJpaEntity);
        tokenMap.put(REFRESH.getName(), refreshToken);

        return tokenMap;
    }
}
