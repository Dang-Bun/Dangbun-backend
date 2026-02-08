package com.dangbun.domain.member.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 GetMemberForCleaningQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeGetMemberForCleaningQuery implements GetMemberForCleaningQuery {

    private final Map<String, MemberInfo> storageByName = new HashMap<>();

    @Override
    public List<MemberInfo> findAllByNameIn(List<String> names) {
        return names.stream()
                .filter(storageByName::containsKey)
                .map(storageByName::get)
                .toList();
    }

    public void addMember(Long memberId, String name) {
        storageByName.put(name, new MemberInfo(memberId, name));
    }

    public void clear() {
        storageByName.clear();
    }

    public int count() {
        return storageByName.size();
    }
}
