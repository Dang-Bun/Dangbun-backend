package com.dangbun.domain.duty.adapter.out.persistence;

import com.dangbun.domain.duty.application.port.out.DutyCommandPort;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 인메모리 Duty 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeDutyRepository implements DutyCommandPort, DutyQueryPort {

    private final Map<Long, Duty> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Duty save(Duty duty) {
        if (duty.getDutyId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Duty saved = Duty.withId(new Duty.DutyId(newId), duty.getName(), duty.getIcon(), duty.getPlaceId());
            storage.put(newId, saved);
            return saved;
        } else {
            Long existingId = duty.getDutyId().value();
            storage.put(existingId, duty);
            // idGenerator를 기존 ID보다 큰 값으로 유지
            if (existingId >= idGenerator.get()) {
                idGenerator.set(existingId + 1);
            }
            return duty;
        }
    }

    @Override
    public void delete(Duty duty) {
        storage.remove(duty.getDutyId().value());
    }

    @Override
    public List<Duty> findByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(d -> d.getPlaceId().equals(placeId))
                .toList();
    }

    @Override
    public Optional<Duty> findById(Long dutyId) {
        return Optional.ofNullable(storage.get(dutyId));
    }

    @Override
    public Optional<Duty> findByIdAndPlaceId(Long dutyId, Long placeId) {
        return storage.values().stream()
                .filter(d -> d.getDutyId().value().equals(dutyId) && d.getPlaceId().equals(placeId))
                .findFirst();
    }

    @Override
    public boolean existsByNameAndPlaceId(String name, Long placeId) {
        return storage.values().stream()
                .anyMatch(d -> d.getName().equals(name) && d.getPlaceId().equals(placeId));
    }

    @Override
    public String getDutyNameById(Long dutyId) {
        Duty duty = storage.get(dutyId);
        return duty != null ? duty.getName() : null;
    }

    @Override
    public List<Duty> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Duty> findDistinctDutiesByMemberIds(List<Long> memberIds) {
        // 테스트용 단순 구현 - 실제 로직은 MemberDuty 조인이 필요
        return new ArrayList<>(storage.values());
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int count() {
        return storage.size();
    }
}
