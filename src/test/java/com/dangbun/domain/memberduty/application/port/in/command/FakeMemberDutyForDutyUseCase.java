package com.dangbun.domain.memberduty.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 MemberDutyForDutyUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberDutyForDutyUseCase implements MemberDutyForDutyUseCase {

    private final Map<Long, List<Long>> dutyMemberMap = new HashMap<>();

    @Override
    public void saveAllByDutyId(Long dutyId, List<Long> memberIds) {
        dutyMemberMap.put(dutyId, new ArrayList<>(memberIds));
    }

    @Override
    public void deleteAllByDutyId(Long dutyId) {
        dutyMemberMap.remove(dutyId);
    }

    public List<Long> getMemberIdsByDutyId(Long dutyId) {
        return dutyMemberMap.getOrDefault(dutyId, List.of());
    }

    public boolean existsAssignment(Long dutyId, Long memberId) {
        List<Long> memberIds = dutyMemberMap.get(dutyId);
        return memberIds != null && memberIds.contains(memberId);
    }

    public void clear() {
        dutyMemberMap.clear();
    }

    public int countDuties() {
        return dutyMemberMap.size();
    }
}
