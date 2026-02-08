package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.application.port.out.AuthCodePort;
import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.INVALID_CERT_CODE;

/**
 * 테스트용 인메모리 인증코드 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeAuthCodePort implements AuthCodePort {

    private final Map<String, String> authCodes = new HashMap<>();
    private final Set<String> sentEmails = new HashSet<>();

    @Override
    public void sendAuthCode(String email) {
        String code = generateCode();
        authCodes.put(email, code);
        sentEmails.add(email);
    }

    @Override
    public void checkAuthCode(String email, String authCode) {
        String storedCode = authCodes.get(email);
        if (storedCode == null || !storedCode.equals(authCode)) {
            throw new InvalidCertCodeException(INVALID_CERT_CODE);
        }
        // 인증 성공시 코드 삭제
        authCodes.remove(email);
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        authCodes.clear();
        sentEmails.clear();
    }

    public void setAuthCode(String email, String code) {
        authCodes.put(email, code);
    }

    public boolean wasSentTo(String email) {
        return sentEmails.contains(email);
    }

    public String getAuthCode(String email) {
        return authCodes.get(email);
    }

    private String generateCode() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
