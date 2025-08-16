package com.dangbun.global.security.refactor;

import com.dangbun.domain.user.entity.User;
import com.dangbun.global.security.TokenProvider;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.dangbun.global.security.refactor.TokenName.*;

@Service
public class JwtService {

    private final TokenProvider tokenProvider;

    public JwtService(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    public Map<String, String> generateToken(User user) {

        Map<String , String> tokenMap = new HashMap<>();

        final String accessToken = tokenProvider.createAccessToken(user);
        tokenMap.put(ACCESS.getName(),accessToken);

        final String refreshToken = tokenProvider.createRefreshToken(user);
        tokenMap.put(REFRESH.getName(), refreshToken);

        return tokenMap;
    }
}
