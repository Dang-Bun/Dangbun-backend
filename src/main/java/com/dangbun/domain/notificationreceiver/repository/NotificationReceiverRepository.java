package com.dangbun.domain.notificationreceiver.repository;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.notification.entity.Notification;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiverId;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationReceiverRepository extends JpaRepository<NotificationReceiver, NotificationReceiverId> {
    Page<NotificationReceiver> findByReceiver_MemberId(Long receiverId, Pageable pageable);

    List<NotificationReceiver> findAllByNotification(Notification notification);

    boolean existsByNotificationAndReceiver(Notification notification, Member receiver);

    @Query("""
                select count(nr) from NotificationReceiver nr
                where nr.receiver.memberId = :memberId
                and nr.isRead = false
            """)
    int countUnreadByMemberId(@Param("memberId") Long memberId);
}
