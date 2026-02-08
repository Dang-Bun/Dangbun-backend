package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMemberCleaningForCleaningQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMemberCleaningForCleaningQuery implements GetMemberCleaningForCleaningQuery {

    private final Map<Long, List<String>> storage = new HashMap<>();

    @Override
    public List<String> findMemberNamesByCleaningId(Long cleaningId) {
        return storage.getOrDefault(cleaningId, Collections.emptyList());
    }

    public void addMemberNames(Long cleaningId, List<String> memberNames) {
        storage.put(cleaningId, new ArrayList<>(memberNames));
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
