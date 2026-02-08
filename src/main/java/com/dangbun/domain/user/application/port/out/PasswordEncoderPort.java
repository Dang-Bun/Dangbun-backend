package com.dangbun.domain.user.application.port.out;

/**
 * 비밀번호 인코딩/검증을 위한 아웃고잉 포트
 */
public interface PasswordEncoderPort {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
