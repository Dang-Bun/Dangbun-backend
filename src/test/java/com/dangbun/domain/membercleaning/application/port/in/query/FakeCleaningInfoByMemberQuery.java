package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 CleaningInfoByMemberQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningInfoByMemberQuery implements GetCleaningInfoByMemberQuery {

    private final Map<Long, List<Long>> memberCleaningMap = new HashMap<>();

    @Override
    public List<Long> getCleaningIdsByMemberId(Long memberId) {
        return memberCleaningMap.getOrDefault(memberId, List.of());
    }

    @Override
    public Integer getCleaningCountByMemberId(Long memberId) {
        return memberCleaningMap.getOrDefault(memberId, List.of()).size();
    }

    public void addCleaningForMember(Long memberId, Long cleaningId) {
        memberCleaningMap.computeIfAbsent(memberId, k -> new ArrayList<>()).add(cleaningId);
    }

    public void setCleaningsForMember(Long memberId, List<Long> cleaningIds) {
        memberCleaningMap.put(memberId, new ArrayList<>(cleaningIds));
    }

    public void clear() {
        memberCleaningMap.clear();
    }
}
