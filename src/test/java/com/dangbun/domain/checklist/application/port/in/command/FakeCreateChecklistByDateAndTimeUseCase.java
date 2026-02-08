package com.dangbun.domain.checklist.application.port.in.command;

import com.dangbun.domain.cleaningdate.domain.CleaningDate;

import java.util.*;

/**
 * 테스트용 인메모리 CreateChecklistByDateAndTimeUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCreateChecklistByDateAndTimeUseCase implements CreateChecklistByDateAndTimeUseCase {

    private final List<CreateChecklistRecord> history = new ArrayList<>();

    @Override
    public void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId) {
        history.add(new CreateChecklistRecord(cleaningId, new ArrayList<>(cleaningDates), placeId));
    }

    public List<CreateChecklistRecord> getHistory() {
        return new ArrayList<>(history);
    }

    public void clear() {
        history.clear();
    }

    public record CreateChecklistRecord(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId) {}
}
