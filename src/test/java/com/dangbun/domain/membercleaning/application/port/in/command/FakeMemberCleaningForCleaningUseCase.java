package com.dangbun.domain.membercleaning.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 MemberCleaningForCleaningUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeMemberCleaningForCleaningUseCase implements MemberCleaningForCleaningUseCase {

    private final Map<Long, List<Long>> storage = new HashMap<>();

    @Override
    public void saveAllByCleaningIdAndMemberIds(Long cleaningId, List<Long> memberIds) {
        storage.put(cleaningId, new ArrayList<>(memberIds));
    }

    @Override
    public void deleteAllByCleaningId(Long cleaningId) {
        storage.remove(cleaningId);
    }

    public List<Long> getMemberIdsByCleaningId(Long cleaningId) {
        return storage.getOrDefault(cleaningId, Collections.emptyList());
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
