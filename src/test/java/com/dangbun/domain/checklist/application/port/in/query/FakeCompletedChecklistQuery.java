package com.dangbun.domain.checklist.application.port.in.query;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 테스트용 인메모리 CompletedChecklistQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCompletedChecklistQuery implements GetCompletedChecklistQuery {

    private final Set<Long> completedCleaningIds = new HashSet<>();

    @Override
    public boolean existsCompletedChecklistByDateAndCleaningId(LocalDateTime start, LocalDateTime end, Long cleaningId) {
        return completedCleaningIds.contains(cleaningId);
    }

    public void markAsCompleted(Long cleaningId) {
        completedCleaningIds.add(cleaningId);
    }

    public void clear() {
        completedCleaningIds.clear();
    }
}
