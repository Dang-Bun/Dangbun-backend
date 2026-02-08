package com.dangbun.domain.cleaning.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 CleaningForDutyUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningForDutyUseCase implements CleaningForDutyUseCase {

    private final Map<Long, Long> cleaningDutyMap = new HashMap<>();
    private final List<AssignmentRecord> assignmentHistory = new ArrayList<>();
    private final List<Long> removeHistory = new ArrayList<>();

    @Override
    public void assignCleaningsToDuty(Long dutyId, List<Long> cleaningIds) {
        for (Long cleaningId : cleaningIds) {
            cleaningDutyMap.put(cleaningId, dutyId);
        }
        assignmentHistory.add(new AssignmentRecord(dutyId, new ArrayList<>(cleaningIds)));
    }

    @Override
    public void removeCleaningFromDuty(Long cleaningId) {
        cleaningDutyMap.remove(cleaningId);
        removeHistory.add(cleaningId);
    }

    public Long getDutyIdByCleaningId(Long cleaningId) {
        return cleaningDutyMap.get(cleaningId);
    }

    public boolean isAssigned(Long cleaningId, Long dutyId) {
        return dutyId.equals(cleaningDutyMap.get(cleaningId));
    }

    public List<AssignmentRecord> getAssignmentHistory() {
        return new ArrayList<>(assignmentHistory);
    }

    public List<Long> getRemoveHistory() {
        return new ArrayList<>(removeHistory);
    }

    public void clear() {
        cleaningDutyMap.clear();
        assignmentHistory.clear();
        removeHistory.clear();
    }

    public record AssignmentRecord(Long dutyId, List<Long> cleaningIds) {}
}
