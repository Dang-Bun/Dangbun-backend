package com.dangbun.domain.cleaning.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetCleaningForDutyQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetCleaningForDutyQuery implements GetCleaningForDutyQuery {

    private final Map<Long, CleaningInfo> storage = new HashMap<>();

    @Override
    public List<CleaningInfo> findAllByDutyId(Long dutyId) {
        return storage.values().stream()
                .filter(c -> dutyId.equals(c.dutyId()))
                .toList();
    }

    @Override
    public List<CleaningInfo> findAllByIds(List<Long> cleaningIds) {
        return cleaningIds.stream()
                .filter(storage::containsKey)
                .map(storage::get)
                .toList();
    }

    @Override
    public Optional<CleaningInfo> findById(Long cleaningId) {
        return Optional.ofNullable(storage.get(cleaningId));
    }

    @Override
    public Optional<CleaningInfo> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId) {
        CleaningInfo cleaning = storage.get(cleaningId);
        if (cleaning != null && dutyId.equals(cleaning.dutyId())) {
            return Optional.of(cleaning);
        }
        return Optional.empty();
    }

    public void addCleaning(Long cleaningId, String name, Long dutyId) {
        storage.put(cleaningId, new CleaningInfo(cleaningId, name, dutyId));
    }

    public void updateDutyId(Long cleaningId, Long dutyId) {
        CleaningInfo existing = storage.get(cleaningId);
        if (existing != null) {
            storage.put(cleaningId, new CleaningInfo(existing.cleaningId(), existing.name(), dutyId));
        }
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
