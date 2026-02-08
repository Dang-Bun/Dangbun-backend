package com.dangbun.domain.user.adapter.out.external.kakao;

import com.dangbun.common.hexagonal.ExternalAdapter;
import com.dangbun.domain.user.client.KakaoApiClient;
import com.dangbun.domain.user.client.KakaoAuthClient;
import com.dangbun.domain.user.adapter.in.web.dto.response.KakaoTokenResponse;
import com.dangbun.domain.user.adapter.in.web.dto.response.KakaoUserResponse;
import com.dangbun.domain.user.application.port.out.KakaoOAuthPort;
import com.dangbun.domain.user.domain.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@ExternalAdapter
@RequiredArgsConstructor
public class KakaoOAuthAdapter implements KakaoOAuthPort {

    private final KakaoAuthClient kakaoAuthClient;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    @Value("${kakao.client-secret.kakao-login}")
    private String clientSecret;

    @Override
    public String fetchAccessToken(String authCode) {
        KakaoTokenResponse response = kakaoAuthClient.fetchKakaoToken(
                "authorization_code",
                clientId,
                redirectUri,
                authCode,
                clientSecret
        );
        return response.access_token();
    }

    @Override
    public KakaoUserInfo fetchUserInfo(String accessToken) {
        String propertyKeys = "[\"kakao_account.email\", \"kakao_account.profile\"]";

        KakaoUserResponse response = kakaoApiClient.getUserInfo(
                "Bearer " + accessToken,
                propertyKeys
        );

        return KakaoUserInfo.of(
                Long.toString(response.id()),
                response.getEmail(),
                response.getNickname()
        );
    }
}
