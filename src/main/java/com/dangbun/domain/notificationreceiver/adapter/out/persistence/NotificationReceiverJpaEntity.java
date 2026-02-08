package com.dangbun.domain.notificationreceiver.adapter.out.persistence;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.notification.adapter.out.persistence.NotificationJpaEntity;
import com.dangbun.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name="notification_receiver")
public class NotificationReceiverJpaEntity extends BaseEntity {

    @EmbeddedId
    private NotificationReceiverId id;

    @MapsId("receiverId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MemberJpaEntity receiver;

    @MapsId("notificationId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NotificationJpaEntity notificationJpaEntity;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;


    @Builder
    public NotificationReceiverJpaEntity(MemberJpaEntity receiver, NotificationJpaEntity notificationJpaEntity, boolean isRead) {
        this.receiver = receiver;
        this.notificationJpaEntity = notificationJpaEntity;
        this.isRead = isRead;
        this.id = new NotificationReceiverId(receiver.getMemberId(), notificationJpaEntity.getNotificationId());
    }


    public void markAsRead() {
        this.isRead=true;
    }
}
