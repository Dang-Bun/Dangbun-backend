package com.dangbun.domain.duty.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetDutyForCleaningQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetDutyForCleaningQuery implements GetDutyForCleaningQuery {

    private final Map<Long, DutyInfo> storage = new HashMap<>();

    @Override
    public Optional<DutyInfo> findById(Long dutyId) {
        return Optional.ofNullable(storage.get(dutyId));
    }

    @Override
    public List<DutyInfo> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<DutyInfo> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        // 테스트용 단순 구현 - 모든 당번 반환
        return new ArrayList<>(storage.values());
    }

    public void addDuty(Long dutyId, String name, String icon) {
        storage.put(dutyId, new DutyInfo(dutyId, name, icon));
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
