package com.dangbun.global.security.refactor;

import com.dangbun.domain.user.entity.User;
import com.dangbun.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import static com.dangbun.global.security.refactor.TokenName.*;

@RequiredArgsConstructor
@Service
public class JwtService {

    private final TokenProvider tokenProvider;


    public Map<String, String> generateToken(User user) {

        Map<String , String> tokenMap = new HashMap<>();

        final String accessToken = tokenProvider.createAccessToken(user);
        tokenMap.put(ACCESS.getName(),accessToken);

        final String refreshToken = tokenProvider.createRefreshToken(user);
        tokenMap.put(REFRESH.getName(), refreshToken);

        return tokenMap;
    }
}
