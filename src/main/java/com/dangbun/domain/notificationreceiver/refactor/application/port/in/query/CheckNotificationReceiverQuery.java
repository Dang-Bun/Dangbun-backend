package com.dangbun.domain.notificationreceiver.refactor.application.port.in.query;

import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.notification.domain.Notification;

public interface CheckNotificationReceiverQuery {
    boolean existsByNotificationJpaEntityAndReceiver(Notification notification, Member member);
}
