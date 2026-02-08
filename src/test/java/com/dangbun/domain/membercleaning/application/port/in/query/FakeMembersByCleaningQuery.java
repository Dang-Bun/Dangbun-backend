package com.dangbun.domain.membercleaning.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

import java.util.*;

/**
 * 테스트용 인메모리 MembersByCleaningQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMembersByCleaningQuery implements GetMembersByCleaningQuery {

    private final Map<Long, List<Member>> storage = new HashMap<>();

    @Override
    public List<Member> getMembersByCleaningId(Long cleaningId) {
        return storage.getOrDefault(cleaningId, List.of());
    }

    public void addMembersForCleaning(Long cleaningId, List<Member> members) {
        storage.put(cleaningId, new ArrayList<>(members));
    }

    public void addMemberForCleaning(Long cleaningId, Member member) {
        storage.computeIfAbsent(cleaningId, k -> new ArrayList<>()).add(member);
    }

    public void clear() {
        storage.clear();
    }
}
