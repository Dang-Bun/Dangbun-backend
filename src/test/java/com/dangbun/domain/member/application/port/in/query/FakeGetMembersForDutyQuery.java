package com.dangbun.domain.member.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMembersForDutyQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMembersForDutyQuery implements GetMembersForDutyQuery {

    private final Map<Long, MemberInfo> storage = new HashMap<>();

    @Override
    public List<MemberInfo> findAllByIds(List<Long> memberIds) {
        return memberIds.stream()
                .filter(storage::containsKey)
                .map(storage::get)
                .toList();
    }

    public void addMember(Long memberId, String name) {
        storage.put(memberId, new MemberInfo(memberId, name));
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
