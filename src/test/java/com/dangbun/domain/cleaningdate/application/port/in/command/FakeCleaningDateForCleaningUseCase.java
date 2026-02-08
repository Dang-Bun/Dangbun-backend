package com.dangbun.domain.cleaningdate.application.port.in.command;

import java.time.LocalDate;
import java.util.*;

/**
 * 테스트용 인메모리 CleaningDateForCleaningUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningDateForCleaningUseCase implements CleaningDateForCleaningUseCase {

    private final Map<Long, List<LocalDate>> storage = new HashMap<>();

    @Override
    public void saveAllByCleaningId(Long cleaningId, List<LocalDate> dates) {
        storage.put(cleaningId, new ArrayList<>(dates));
    }

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        storage.remove(cleaningId);
    }

    public List<LocalDate> getDatesByCleaningId(Long cleaningId) {
        return storage.getOrDefault(cleaningId, Collections.emptyList());
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
