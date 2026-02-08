package com.dangbun.domain.checklist.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetChecklistForCleaningQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetChecklistForCleaningQuery implements GetChecklistForCleaningQuery {

    private final Map<Long, List<Long>> storage = new HashMap<>();

    @Override
    public List<Long> findChecklistIdsByCleaningId(Long cleaningId) {
        return storage.getOrDefault(cleaningId, Collections.emptyList());
    }

    public void addChecklistIds(Long cleaningId, List<Long> checklistIds) {
        storage.put(cleaningId, new ArrayList<>(checklistIds));
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
