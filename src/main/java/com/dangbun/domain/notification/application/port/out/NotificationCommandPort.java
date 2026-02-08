package com.dangbun.domain.notification.application.port.out;

import com.dangbun.domain.notification.domain.Notification;

public interface NotificationCommandPort {

    Notification save(Notification notification);

    void deleteById(Long notificationId);
}
