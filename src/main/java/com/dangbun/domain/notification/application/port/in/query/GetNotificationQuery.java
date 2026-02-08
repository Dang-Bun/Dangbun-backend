package com.dangbun.domain.notification.application.port.in.query;

import com.dangbun.domain.notification.domain.Notification;

public interface GetNotificationQuery {
    Notification getNotification(Long notificationId);
}
