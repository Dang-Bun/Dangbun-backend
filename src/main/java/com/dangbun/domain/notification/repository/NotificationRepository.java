package com.dangbun.domain.notification.repository;

import com.dangbun.domain.notification.entity.Notification;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findBySender_MemberId(Long senderMemberId, Pageable pageable);
}
