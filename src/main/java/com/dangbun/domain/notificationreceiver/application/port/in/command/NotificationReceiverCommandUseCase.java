package com.dangbun.domain.notificationreceiver.application.port.in.command;

import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;

public interface NotificationReceiverCommandUseCase {

    void save(NotificationReceiver notificationReceiver);
    void markAsRead(Long notificationId);
}
