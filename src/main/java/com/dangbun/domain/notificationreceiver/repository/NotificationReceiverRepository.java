package com.dangbun.domain.notificationreceiver.repository;

import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiverId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationReceiverRepository extends JpaRepository<NotificationReceiver, NotificationReceiverId> {

}
