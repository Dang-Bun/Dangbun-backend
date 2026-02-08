package com.dangbun.domain.user.adapter.out.external.kakao;

import com.dangbun.domain.user.client.KakaoApiClient;
import com.dangbun.domain.user.application.port.out.KakaoPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoAdapter implements KakaoPort {

    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Override
    public void unlinkAccount(String socialId) {
        try {
            kakaoApiClient.unlink("KakaoAK " + adminKey, "user_id", Long.parseLong(socialId));
        } catch (feign.FeignException e) {
            // -101 에러는 이미 연결 해제된 경우이므로 무시
            if (!e.contentUTF8().contains("-101")) {
                throw e;
            }
        }
    }

    @Override
    public void logout(String socialId) {
        try {
            kakaoApiClient.logout("KakaoAK " + adminKey, "user_id", Long.parseLong(socialId));
        } catch (feign.FeignException e) {
            // 로그아웃 실패는 무시 (이미 로그아웃된 경우 등)
        }
    }
}
