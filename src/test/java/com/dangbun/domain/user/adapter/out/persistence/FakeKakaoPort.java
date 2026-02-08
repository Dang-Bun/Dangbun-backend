package com.dangbun.domain.user.adapter.out.persistence;

import com.dangbun.domain.user.application.port.out.KakaoPort;

import java.util.HashSet;
import java.util.Set;

/**
 * 테스트용 인메모리 카카오 API 연동
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeKakaoPort implements KakaoPort {

    private final Set<String> unlinkedAccounts = new HashSet<>();
    private final Set<String> loggedOutAccounts = new HashSet<>();

    @Override
    public void unlinkAccount(String socialId) {
        unlinkedAccounts.add(socialId);
    }

    @Override
    public void logout(String socialId) {
        loggedOutAccounts.add(socialId);
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        unlinkedAccounts.clear();
        loggedOutAccounts.clear();
    }

    public boolean wasUnlinked(String socialId) {
        return unlinkedAccounts.contains(socialId);
    }

    public boolean wasLoggedOut(String socialId) {
        return loggedOutAccounts.contains(socialId);
    }
}
