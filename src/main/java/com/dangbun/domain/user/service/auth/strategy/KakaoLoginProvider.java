package com.dangbun.domain.user.service.auth.strategy;


import com.dangbun.domain.user.*;
import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.request.auth.PostKakaoLoginRequest;
import com.dangbun.domain.user.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.dangbun.global.security.jwt.TokenPrefix.ACCESS;
import static com.dangbun.global.security.jwt.TokenPrefix.REFRESH;

@Component
@RequiredArgsConstructor
public class KakaoLoginProvider implements LoginProvider {

    private final ExternalUserClient externalUserClient;
    private final KakaoApiClient kakaoApiClient;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthRedisService authRedisService;

    String code;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret.kakao-login}")
    private String clientSecret;


    @Override
    public boolean supports(LoginType type) {
        return type.equals(LoginType.KAKAO);
    }

    @Transactional
    @Override
    public PostUserLoginResponse login(LoginRequest loginRequest) {
        if (!(loginRequest instanceof PostKakaoLoginRequest request)) {
            throw new IllegalArgumentException("Invalid login request type");
        }

        KakaoTokenResponse response = externalUserClient.fetchKakaoToken("authorization_code",
                clientId,
                redirectUri,
                request.code(),
                clientSecret);

        String propertyKeys = "[\"kakao_account.email\", \"kakao_account.profile\"]";

        System.out.printf(response.toString());

        KakaoUserResponse userInfo = kakaoApiClient.getUserInfo("Bearer " + response.access_token(), propertyKeys);

        System.out.println(userInfo.toString());
        User user = userRepository.findBySocialId(Long.toString(userInfo.id())).orElseGet(() ->
                signup(userInfo.getNickname(),
                        userInfo.getEmail(),
                        userInfo.id()));


        Map<String, String> tokenMap = jwtService.generateToken(user);
        authRedisService.saveRefreshToken(user.getUserId(), tokenMap.get(REFRESH.getName()));

        return new PostUserLoginResponse(tokenMap.get(ACCESS.getName()), tokenMap.get(REFRESH.getName()));
    }

    @Transactional
    protected User signup(String nickname, String email, Long id) {
        User user = User.builder()
                .name(nickname)
                .email(email)
                .loginType(LoginType.KAKAO)
                .socialId(Long.toString(id))
                .enabled(true)
                .build();

        userRepository.save(user);

        return user;
    }
}
