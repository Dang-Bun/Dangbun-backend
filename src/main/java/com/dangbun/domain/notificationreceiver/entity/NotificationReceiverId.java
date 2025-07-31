package com.dangbun.domain.notificationreceiver.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class NotificationReceiverId {
    @Column(name = "receiver_id")
    private Long receiverId;

    @Column(name = "notification_id")
    private Long notificationId;

    public NotificationReceiverId (Long receiverId, Long notificationId) {
        this.receiverId = receiverId;
        this.notificationId = notificationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof com.dangbun.domain.notificationreceiver.entity.NotificationReceiverId that)) return false;
        return Objects.equals(receiverId, that.receiverId) && Objects.equals(notificationId, that.notificationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(receiverId, notificationId);
    }
}


