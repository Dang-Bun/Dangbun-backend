package com.dangbun.domain.notificationreceiver.application.port.in.query;

import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;

import java.util.List;

public interface GetNotificationReceiverQuery {
    List<NotificationReceiver> findAllByNotificationId(Long notificationId);
}
