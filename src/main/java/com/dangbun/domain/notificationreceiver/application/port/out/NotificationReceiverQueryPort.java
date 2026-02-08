package com.dangbun.domain.notificationreceiver.application.port.out;

import com.dangbun.domain.notificationreceiver.domain.NotificationReceiver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface NotificationReceiverQueryPort {

    Page<NotificationReceiver> findByReceiverId(Long receiverId, Pageable pageable);

    List<NotificationReceiver> findAllByNotificationId(Long notificationId);

    Optional<NotificationReceiver> findByNotificationIdAndReceiverId(Long notificationId, Long receiverId);

    boolean existsByNotificationIdAndReceiverId(Long notificationId, Long receiverId);

    int countUnreadByMemberId(Long memberId);

    List<String> findReceiverNamesByNotificationId(Long notificationId);
}
