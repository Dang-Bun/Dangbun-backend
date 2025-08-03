package com.dangbun.domain.notificationreceiver.dto.response;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.notification.entity.Notification;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiver;
import com.dangbun.domain.notificationreceiver.entity.NotificationReceiverId;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record GetNotificationReceivedListResponse(
        @Schema(description = "받은 알림 목록")
        List<NotificationReceiverDto> notifications,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static GetNotificationReceivedListResponse of (List<NotificationReceiverDto> notifications, boolean hasNext) {
        return new GetNotificationReceivedListResponse(notifications, hasNext);
    }

    public record NotificationReceiverDto(
            @Schema(description = "알림 수신 ID (복합키 : 수신자 id, 알림 id)", example = "{ \"receiverId\": 5, \"notificationId\": 12 }")
            NotificationReceiverId notificationReceiverId,

            @Schema(description = "발송자 이름", example = "지윤")
            String senderName,

            @Schema(description = "알림 제목", example = "안녕하세요.")
            String title,

            @Schema(description = "알림 내용", example = "안녕하세요. 요즘 날씨가 매우 더운데...")
            String content,

            @Schema(description = "알림 발송 시각", example = "3시간 전 또는 2025-08-03 15:20")
            String createdAt,

            @Schema(description = "알림 읽음 여부", example = "true")
            Boolean isRead
    ) {
        public static NotificationReceiverDto of(NotificationReceiver receiver) {
            Notification notification = receiver.getNotification();
            return new NotificationReceiverDto(
                    receiver.getId(),
                    notification.getSender().getName(),
                    notification.getTitle(),
                    notification.getContent(),
                    formatCreatedAt(notification.getCreatedAt()),
                    receiver.isRead()
            );
        }

        private static String formatCreatedAt(LocalDateTime createdAt) {
            Duration duration = Duration.between(createdAt, LocalDateTime.now());
            if (duration.toHours() < 24) {
                if (duration.toMinutes() < 1) return "방금 전";
                if (duration.toHours() < 1) return duration.toMinutes() + "분 전";
                return duration.toHours() + "시간 전";
            }
            return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        }
    }
}


