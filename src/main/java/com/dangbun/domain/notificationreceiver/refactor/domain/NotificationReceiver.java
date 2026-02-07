package com.dangbun.domain.notificationreceiver.refactor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class NotificationReceiver {

    private final NotificationReceiverId notificationReceiverId;
    private final Long receiverId;
    private final Long notificationId;
    private final boolean isRead;
    private final LocalDateTime createdAt;

//    public static NotificationReceiver withoutId(Long receiverId, Long notificationId) {
//        return new NotificationReceiver(
//                new NotificationReceiverId(receiverId, notificationId),
//                receiverId,
//                notificationId,
//                false,
//                null
//        );
//    }

    public static NotificationReceiver of(Long receiverId, Long notificationId, boolean isRead, LocalDateTime createdAt) {
        return new NotificationReceiver(
                new NotificationReceiverId(receiverId, notificationId),
                receiverId,
                notificationId,
                isRead,
                createdAt
        );
    }

    public NotificationReceiver markAsRead() {
        return new NotificationReceiver(
                this.notificationReceiverId,
                this.receiverId,
                this.notificationId,
                true,
                this.createdAt
        );
    }

    public record NotificationReceiverId(Long receiverId, Long notificationId) {
    }
}
