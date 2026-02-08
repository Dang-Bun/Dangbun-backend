package com.dangbun.domain.notification.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, Long> {
    Page<NotificationJpaEntity> findBySender_MemberId(Long senderMemberId, Pageable pageable);
}
