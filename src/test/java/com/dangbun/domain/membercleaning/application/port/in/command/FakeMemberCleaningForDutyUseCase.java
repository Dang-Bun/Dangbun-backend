package com.dangbun.domain.membercleaning.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 MemberCleaningForDutyUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberCleaningForDutyUseCase implements MemberCleaningForDutyUseCase {

    private final Map<Long, List<Long>> cleaningMemberMap = new HashMap<>();

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        cleaningMemberMap.remove(cleaningId);
    }

    @Override
    public void saveAllByCleaningIdAndMemberIds(Long cleaningId, List<Long> memberIds) {
        cleaningMemberMap.put(cleaningId, new ArrayList<>(memberIds));
    }

    public List<Long> getMemberIdsByCleaningId(Long cleaningId) {
        return cleaningMemberMap.getOrDefault(cleaningId, List.of());
    }

    public boolean existsAssignment(Long cleaningId, Long memberId) {
        List<Long> memberIds = cleaningMemberMap.get(cleaningId);
        return memberIds != null && memberIds.contains(memberId);
    }

    public int getAssignedMemberCount(Long cleaningId) {
        List<Long> memberIds = cleaningMemberMap.get(cleaningId);
        return memberIds != null ? memberIds.size() : 0;
    }

    public void clear() {
        cleaningMemberMap.clear();
    }

    public int countCleanings() {
        return cleaningMemberMap.size();
    }
}
