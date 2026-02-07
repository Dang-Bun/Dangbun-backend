package com.dangbun.domain.notificationreceiver.refactor.application.port.in.command;

import com.dangbun.domain.notificationreceiver.refactor.domain.NotificationReceiver;

public interface NotificationReceiverCommandUseCase {

    void save(NotificationReceiver notificationReceiver);
    void markAsRead(Long notificationId);
}
