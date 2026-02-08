package com.dangbun.domain.cleaning.application.port.out;

import com.dangbun.domain.cleaning.domain.Cleaning;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 인메모리 Cleaning 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningRepository implements CleaningCommandPort, CleaningQueryPort {

    private final Map<Long, Cleaning> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Cleaning save(Cleaning cleaning) {
        if (cleaning.getCleaningId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Cleaning saved = Cleaning.withId(
                    new Cleaning.CleaningId(newId),
                    cleaning.getName(),
                    cleaning.getRepeatType(),
                    cleaning.getRepeatDays(),
                    cleaning.getDutyId(),
                    cleaning.getNeedPhoto(),
                    cleaning.getPlaceId()
            );
            storage.put(newId, saved);
            return saved;
        } else {
            Long existingId = cleaning.getCleaningId().value();
            storage.put(existingId, cleaning);
            if (existingId >= idGenerator.get()) {
                idGenerator.set(existingId + 1);
            }
            return cleaning;
        }
    }

    @Override
    public void saveAll(List<Cleaning> cleanings) {
        for (Cleaning cleaning : cleanings) {
            save(cleaning);
        }
    }

    @Override
    public void delete(Cleaning cleaning) {
        storage.remove(cleaning.getCleaningId().value());
    }

    @Override
    public void deleteById(Long cleaningId) {
        storage.remove(cleaningId);
    }

    @Override
    public Optional<Cleaning> findById(Long cleaningId) {
        return Optional.ofNullable(storage.get(cleaningId));
    }

    @Override
    public Optional<Cleaning> findWithDutyNullableById(Long cleaningId) {
        return Optional.ofNullable(storage.get(cleaningId));
    }

    @Override
    public Optional<Cleaning> findByCleaningIdAndDutyId(Long cleaningId, Long dutyId) {
        Cleaning cleaning = storage.get(cleaningId);
        if (cleaning != null && Objects.equals(dutyId, cleaning.getDutyId())) {
            return Optional.of(cleaning);
        }
        return Optional.empty();
    }

    @Override
    public List<Cleaning> findAllByDutyId(Long dutyId) {
        return storage.values().stream()
                .filter(c -> Objects.equals(dutyId, c.getDutyId()))
                .toList();
    }

    @Override
    public List<Cleaning> findByDutyIdAndMemberIds(Long dutyId, List<Long> memberIds) {
        // 테스트용 단순 구현 - 실제 로직은 MemberCleaning 조인 필요
        return storage.values().stream()
                .filter(c -> Objects.equals(dutyId, c.getDutyId()))
                .toList();
    }

    @Override
    public List<Cleaning> findUnassignedCleaningsByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(c -> c.getPlaceId().equals(placeId) && c.getDutyId() == null)
                .toList();
    }

    @Override
    public List<Cleaning> findByPlaceId(Long placeId) {
        return storage.values().stream()
                .filter(c -> c.getPlaceId().equals(placeId))
                .toList();
    }

    @Override
    public boolean existsByNameAndDutyIdAndPlaceId(String name, Long dutyId, Long placeId) {
        return storage.values().stream()
                .anyMatch(c -> c.getName().equals(name)
                        && Objects.equals(c.getDutyId(), dutyId)
                        && c.getPlaceId().equals(placeId));
    }

    @Override
    public boolean existsByNameAndDutyIdAndCleaningIdNotAndPlaceId(String name, Long dutyId, Long cleaningId, Long placeId) {
        return storage.values().stream()
                .anyMatch(c -> c.getName().equals(name)
                        && Objects.equals(c.getDutyId(), dutyId)
                        && !c.getCleaningId().value().equals(cleaningId)
                        && c.getPlaceId().equals(placeId));
    }

    @Override
    public List<Cleaning> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public List<Cleaning> findAllByIds(List<Long> cleaningIds) {
        return cleaningIds.stream()
                .filter(storage::containsKey)
                .map(storage::get)
                .toList();
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int count() {
        return storage.size();
    }

    public void setIdGenerator(long value) {
        idGenerator.set(value);
    }
}
