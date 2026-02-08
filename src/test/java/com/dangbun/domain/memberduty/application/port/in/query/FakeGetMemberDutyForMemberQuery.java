package com.dangbun.domain.memberduty.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMemberDutyForMemberQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMemberDutyForMemberQuery implements GetMemberDutyForMemberQuery {

    private final Map<Long, List<DutyInfo>> memberDuties = new HashMap<>();
    private final Set<MemberDutyKey> existingMemberDuties = new HashSet<>();

    @Override
    public List<DutyInfo> findDutyInfosByMemberId(Long memberId) {
        return memberDuties.getOrDefault(memberId, Collections.emptyList());
    }

    @Override
    public boolean existsByDutyIdAndMemberId(Long dutyId, Long memberId) {
        return existingMemberDuties.contains(new MemberDutyKey(memberId, dutyId));
    }

    public void addDutyInfoForMember(Long memberId, Long dutyId, String dutyName) {
        memberDuties.computeIfAbsent(memberId, k -> new ArrayList<>())
                .add(new DutyInfo(dutyId, dutyName));
        existingMemberDuties.add(new MemberDutyKey(memberId, dutyId));
    }

    public void addMemberDutyAssignment(Long memberId, Long dutyId) {
        existingMemberDuties.add(new MemberDutyKey(memberId, dutyId));
    }

    public void clear() {
        memberDuties.clear();
        existingMemberDuties.clear();
    }

    public int countDutyInfos() {
        return memberDuties.values().stream().mapToInt(List::size).sum();
    }

    public int countAssignments() {
        return existingMemberDuties.size();
    }

    private record MemberDutyKey(Long memberId, Long dutyId) {}
}
