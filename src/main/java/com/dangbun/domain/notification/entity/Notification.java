package com.dangbun.domain.notification.entity;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.global.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @Builder
    public Notification(Long notificationId, String title, String content, LocalDateTime createAt, Member sender) {
        this.notificationId = notificationId;
        this.title = title;
        this.content = content;
        this.sender = sender;
    }
}
