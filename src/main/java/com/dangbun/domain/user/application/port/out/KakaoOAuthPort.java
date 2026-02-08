package com.dangbun.domain.user.application.port.out;

import com.dangbun.domain.user.domain.KakaoUserInfo;

/**
 * 카카오 OAuth 연동을 위한 아웃고잉 포트
 */
public interface KakaoOAuthPort {

    String fetchAccessToken(String authCode);

    KakaoUserInfo fetchUserInfo(String accessToken);
}
