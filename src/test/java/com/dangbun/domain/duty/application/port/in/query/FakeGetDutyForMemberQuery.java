package com.dangbun.domain.duty.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetDutyForMemberQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetDutyForMemberQuery implements GetDutyForMemberQuery {

    private final Map<DutyKey, DutyInfo> storage = new HashMap<>();

    @Override
    public Optional<DutyInfo> findByIdAndPlaceId(Long dutyId, Long placeId) {
        return Optional.ofNullable(storage.get(new DutyKey(dutyId, placeId)));
    }

    public void addDuty(Long dutyId, Long placeId, String name) {
        storage.put(new DutyKey(dutyId, placeId), new DutyInfo(dutyId, name));
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }

    private record DutyKey(Long dutyId, Long placeId) {}
}
