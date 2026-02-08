package com.dangbun.domain.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Notification {

    private final NotificationId notificationId;
    private final NotificationTemplate template;
    private final String title;
    private final String content;
    private final Long senderId;
    private final String senderName;
    private final LocalDateTime createdAt;

    public static Notification withoutId(NotificationTemplate template, String title, String content, Long senderId, String senderName) {
        return new Notification(null, template, title, content, senderId, senderName, null);
    }

    public static Notification withId(NotificationId notificationId, NotificationTemplate template, String title, String content, Long senderId, String senderName, LocalDateTime createdAt) {
        return new Notification(notificationId, template, title, content, senderId, senderName, createdAt);
    }

    public record NotificationId(Long value) {
    }
}
