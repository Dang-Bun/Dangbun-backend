package com.dangbun.domain.notificationreceiver.application.port.in.query;

public interface GetUnreadNotificationCountQuery {

    Integer getUnreadCountByMemberId(Long memberId);
}
