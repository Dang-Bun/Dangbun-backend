package com.dangbun.domain.member.application.port.in.query;

import com.dangbun.domain.member.domain.Member;

import java.util.*;

/**
 * 테스트용 인메모리 MembersByUserIdQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMembersByUserIdQuery implements GetMembersByUserIdQuery {

    private final List<Member> storage = new ArrayList<>();

    @Override
    public List<Member> getMembersByUserId(Long userId) {
        return storage.stream()
                .filter(m -> m.getUserId().equals(userId))
                .toList();
    }

    @Override
    public Optional<Member> getMemberByUserIdAndPlaceId(Long userId, Long placeId) {
        return storage.stream()
                .filter(m -> m.getUserId().equals(userId) && m.getPlaceId().equals(placeId))
                .findFirst();
    }

    @Override
    public Optional<Member> getFirstMemberByPlaceId(Long placeId) {
        return storage.stream()
                .filter(m -> m.getPlaceId().equals(placeId))
                .findFirst();
    }

    public void addMember(Member member) {
        storage.add(member);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
