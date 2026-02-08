package com.dangbun.domain.notificationreceiver.application.port.out;

import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;

public interface NotificationReceiverCommandPort {

    NotificationReceiver save(NotificationReceiver notificationReceiver);

    void markAsRead(Long notificationId, Long receiverId);

    void deleteByNotificationId(Long notificationId);
}
