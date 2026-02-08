package com.dangbun.domain.notificationreceiver.adapter.out.persistence;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.notification.adapter.out.persistence.NotificationJpaEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationReceiverRepository extends JpaRepository<NotificationReceiverJpaEntity, NotificationReceiverId> {
    Page<NotificationReceiverJpaEntity> findByReceiver_MemberId(Long receiverId, Pageable pageable);

    List<NotificationReceiverJpaEntity> findAllByNotificationJpaEntity(NotificationJpaEntity notificationJpaEntity);

    boolean existsByNotificationJpaEntityAndReceiver(NotificationJpaEntity notificationJpaEntity, MemberJpaEntity receiver);

    @Query("""
                select count(nr) from NotificationReceiverJpaEntity nr
                where nr.receiver.memberId = :memberId
                and nr.isRead = false
            """)
    int countUnreadByMemberId(@Param("memberId") Long memberId);

    Optional<NotificationReceiverJpaEntity> findByNotificationJpaEntity_NotificationIdAndReceiver_MemberId(Long notificationId, Long memberId);
}
