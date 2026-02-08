package com.dangbun.domain.memberduty.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMemberDutyForDutyQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMemberDutyForDutyQuery implements GetMemberDutyForDutyQuery {

    private final Map<Long, List<Long>> dutyMemberIdsMap = new HashMap<>();
    private final Map<Long, List<MemberDutyMemberInfo>> dutyMemberInfosMap = new HashMap<>();

    @Override
    public List<Long> findMemberIdsByDutyId(Long dutyId) {
        return dutyMemberIdsMap.getOrDefault(dutyId, List.of());
    }

    @Override
    public List<MemberDutyMemberInfo> findMemberInfosByDutyId(Long dutyId) {
        return dutyMemberInfosMap.getOrDefault(dutyId, List.of());
    }

    public void addMemberIds(Long dutyId, List<Long> memberIds) {
        dutyMemberIdsMap.put(dutyId, new ArrayList<>(memberIds));
    }

    public void addMemberInfos(Long dutyId, List<MemberDutyMemberInfo> memberInfos) {
        dutyMemberInfosMap.put(dutyId, new ArrayList<>(memberInfos));
    }

    public void addMemberInfo(Long dutyId, Long memberId, String role, String name) {
        dutyMemberInfosMap.computeIfAbsent(dutyId, k -> new ArrayList<>())
                .add(new MemberDutyMemberInfo(memberId, role, name));
    }

    public void clear() {
        dutyMemberIdsMap.clear();
        dutyMemberInfosMap.clear();
    }
}
