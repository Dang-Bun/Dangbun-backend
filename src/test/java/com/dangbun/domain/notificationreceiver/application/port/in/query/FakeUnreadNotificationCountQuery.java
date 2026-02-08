package com.dangbun.domain.notificationreceiver.application.port.in.query;

import java.util.*;

/**
 * 테스트용 인메모리 UnreadNotificationCountQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeUnreadNotificationCountQuery implements GetUnreadNotificationCountQuery {

    private final Map<Long, Integer> unreadCounts = new HashMap<>();

    @Override
    public Integer getUnreadCountByMemberId(Long memberId) {
        return unreadCounts.getOrDefault(memberId, 0);
    }

    public void setUnreadCount(Long memberId, Integer count) {
        unreadCounts.put(memberId, count);
    }

    public void clear() {
        unreadCounts.clear();
    }
}
