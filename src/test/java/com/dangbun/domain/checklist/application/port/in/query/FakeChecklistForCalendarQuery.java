package com.dangbun.domain.checklist.application.port.in.query;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 테스트용 인메모리 ChecklistForCalendarQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeChecklistForCalendarQuery implements GetChecklistForCalendarQuery {

    private final List<ChecklistCalendarInfo> storage = new ArrayList<>();

    @Override
    public List<ChecklistCalendarInfo> findAllByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId) {
        return storage.stream()
                .filter(cl -> {
                    LocalDateTime createdAt = cl.createdAt();
                    return createdAt != null &&
                            !createdAt.isBefore(start) &&
                            createdAt.isBefore(end);
                })
                .toList();
    }

    @Override
    public List<ChecklistCalendarInfo> findByPlaceAndMonth(Long placeId, LocalDateTime start, LocalDateTime end) {
        return findAllByCreatedDateAndPlaceId(start, end, placeId);
    }

    @Override
    public Optional<ChecklistWithCleaningInfo> findWithCleaningById(Long checklistId) {
        return storage.stream()
                .filter(cl -> cl.checklistId().equals(checklistId))
                .map(cl -> new ChecklistWithCleaningInfo(cl.checklistId(), cl.cleaningId(), cl.needPhoto()))
                .findFirst();
    }

    @Override
    public Optional<ChecklistWithCleaningAndDutyInfo> findWithCleaningAndDutyById(Long checklistId) {
        return storage.stream()
                .filter(cl -> cl.checklistId().equals(checklistId))
                .map(cl -> new ChecklistWithCleaningAndDutyInfo(
                        cl.checklistId(),
                        cl.cleaningId(),
                        cl.cleaningName(),
                        cl.dutyName(),
                        cl.needPhoto(),
                        null,
                        null
                ))
                .findFirst();
    }

    public void addChecklist(ChecklistCalendarInfo checklist) {
        storage.add(checklist);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
