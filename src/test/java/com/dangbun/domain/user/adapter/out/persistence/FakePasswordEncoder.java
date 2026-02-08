package com.dangbun.domain.user.adapter.out.persistence;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 테스트용 PasswordEncoder
 * Mock 대신 실제 동작을 시뮬레이션
 * 테스트 목적으로 단순 prefix 방식 사용
 */
public class FakePasswordEncoder implements PasswordEncoder {

    private static final String ENCODED_PREFIX = "encoded_";

    @Override
    public String encode(CharSequence rawPassword) {
        return ENCODED_PREFIX + rawPassword;
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return encodedPassword.equals(ENCODED_PREFIX + rawPassword);
    }
}
