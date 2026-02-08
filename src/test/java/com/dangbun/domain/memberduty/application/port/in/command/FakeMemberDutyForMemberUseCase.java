package com.dangbun.domain.memberduty.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 MemberDutyForMemberUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberDutyForMemberUseCase implements MemberDutyForMemberUseCase {

    private final Set<MemberDutyKey> savedMemberDuties = new HashSet<>();

    @Override
    public void saveMemberDuty(Long memberId, Long dutyId) {
        savedMemberDuties.add(new MemberDutyKey(memberId, dutyId));
    }

    public boolean existsByMemberIdAndDutyId(Long memberId, Long dutyId) {
        return savedMemberDuties.contains(new MemberDutyKey(memberId, dutyId));
    }

    public void clear() {
        savedMemberDuties.clear();
    }

    public int count() {
        return savedMemberDuties.size();
    }

    public Set<MemberDutyKey> getAll() {
        return new HashSet<>(savedMemberDuties);
    }

    public record MemberDutyKey(Long memberId, Long dutyId) {}
}
