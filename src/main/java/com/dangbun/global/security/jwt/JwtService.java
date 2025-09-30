package com.dangbun.global.security.jwt;

import com.dangbun.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.dangbun.global.security.jwt.TokenPrefix.*;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final JwtProvider jwtProvider;


    public Map<String, String> generateToken(User user) {

        Map<String , String> tokenMap = new HashMap<>();

        final String accessToken = jwtProvider.createAccessToken(user.getEmail());
        tokenMap.put(ACCESS.getName(),accessToken);

        final String refreshToken = jwtProvider.createRefreshToken(user);
        tokenMap.put(REFRESH.getName(), refreshToken);

        return tokenMap;
    }
}
