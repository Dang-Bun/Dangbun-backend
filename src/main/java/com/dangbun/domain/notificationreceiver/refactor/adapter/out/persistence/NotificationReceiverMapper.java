package com.dangbun.domain.notificationreceiver.refactor.adapter.out.persistence;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.notification.adapter.out.persistence.NotificationJpaEntity;
import com.dangbun.domain.notification.adapter.out.persistence.SpringDataNotificationRepository;
import com.dangbun.domain.notificationreceiver.refactor.domain.NotificationReceiver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NotificationReceiverMapper {

    /*
     * TODO: Member/Notification 도메인 헥사고날 아키텍처 전환 완료 시 수정
     * MemberRepository -> MemberQueryPort
     * SpringDataNotificationRepository -> NotificationQueryPort
     */
    private final MemberRepository memberRepository;
    private final SpringDataNotificationRepository notificationRepository;

    NotificationReceiver mapToDomainEntity(NotificationReceiverJpaEntity jpaEntity) {
        return NotificationReceiver.of(
                jpaEntity.getReceiver().getMemberId(),
                jpaEntity.getNotificationJpaEntity().getNotificationId(),
                jpaEntity.isRead(),
                jpaEntity.getCreatedAt()
        );
    }

    NotificationReceiverJpaEntity mapToJpaEntity(NotificationReceiver domain) {
        MemberJpaEntity receiver = memberRepository.findById(domain.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + domain.getReceiverId()));

        NotificationJpaEntity notification = notificationRepository.findById(domain.getNotificationId())
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + domain.getNotificationId()));

        return NotificationReceiverJpaEntity.builder()
                .receiver(receiver)
                .notificationJpaEntity(notification)
                .isRead(domain.isRead())
                .build();
    }
}
