package com.dangbun.domain.user.application.port.out;

/**
 * 카카오 API 연동을 위한 아웃고잉 포트
 */
public interface KakaoPort {

    void unlinkAccount(String socialId);

    void logout(String socialId);
}
