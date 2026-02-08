package com.dangbun.domain.membercleaning.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMemberCleaningForDutyQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMemberCleaningForDutyQuery implements GetMemberCleaningForDutyQuery {

    private final Map<Long, List<String>> cleaningMemberNamesMap = new HashMap<>();
    private final Map<Long, Integer> cleaningMemberCountMap = new HashMap<>();

    @Override
    public List<String> findMemberNamesByCleaningId(Long cleaningId) {
        return cleaningMemberNamesMap.getOrDefault(cleaningId, List.of());
    }

    @Override
    public Integer countMembersByCleaningId(Long cleaningId) {
        return cleaningMemberCountMap.getOrDefault(cleaningId, 0);
    }

    public void addMemberNames(Long cleaningId, List<String> memberNames) {
        cleaningMemberNamesMap.put(cleaningId, new ArrayList<>(memberNames));
        cleaningMemberCountMap.put(cleaningId, memberNames.size());
    }

    public void setMemberCount(Long cleaningId, int count) {
        cleaningMemberCountMap.put(cleaningId, count);
    }

    public void clear() {
        cleaningMemberNamesMap.clear();
        cleaningMemberCountMap.clear();
    }
}
