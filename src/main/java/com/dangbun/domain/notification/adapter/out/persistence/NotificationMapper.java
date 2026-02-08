package com.dangbun.domain.notification.adapter.out.persistence;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class NotificationMapper {

    /*
     * TODO: Member 도메인 헥사고날 아키텍처 전환 완료 후 수정
     * MemberRepository -> SpringDataMemberRepository
     */
    private final MemberRepository memberRepository;

    public NotificationJpaEntity mapToJpaEntity(Notification notification) {
        MemberJpaEntity sender = memberRepository.getReferenceById(notification.getSenderId());

        NotificationJpaEntity.NotificationJpaEntityBuilder builder = NotificationJpaEntity.builder()
                .template(notification.getTemplate())
                .title(notification.getTitle())
                .content(notification.getContent())
                .sender(sender);

        if (notification.getNotificationId() != null) {
            builder.notificationId(notification.getNotificationId().value());
        }

        return builder.build();
    }

    public Notification mapToDomainEntity(NotificationJpaEntity entity) {
        return Notification.withId(
                new Notification.NotificationId(entity.getNotificationId()),
                entity.getTemplate(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSender().getMemberId(),
                entity.getSender().getName(),
                entity.getCreatedAt()
        );
    }
}
