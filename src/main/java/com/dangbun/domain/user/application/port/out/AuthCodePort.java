package com.dangbun.domain.user.application.port.out;

/**
 * 인증 코드 관련 아웃고잉 포트
 */
public interface AuthCodePort {

    void sendAuthCode(String email);

    void checkAuthCode(String email, String authCode);
}
