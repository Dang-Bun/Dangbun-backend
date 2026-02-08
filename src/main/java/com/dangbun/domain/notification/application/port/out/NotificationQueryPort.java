package com.dangbun.domain.notification.application.port.out;

import com.dangbun.domain.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface NotificationQueryPort {

    Optional<Notification> findById(Long notificationId);

    Page<Notification> findBySenderId(Long senderId, Pageable pageable);
}
