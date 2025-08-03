package com.dangbun.domain.notification.dto.response;

import com.dangbun.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public record GetNotificationListResponse(
        @Schema(description = "보낸 알림 목록")
        List<NotificationDto> notifications,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public static GetNotificationListResponse of (List<NotificationDto> notifications, boolean hasNext) {
        return new GetNotificationListResponse(notifications, hasNext);
    }

    public record NotificationDto(
            @Schema(description = "알림 ID", example = "1")
            Long notificationId,

            @Schema(description = "발송자 이름", example = "지윤")
            String senderName,

            @Schema(description = "알림 제목", example = "안녕하세요.")
            String title,

            @Schema(description = "알림 내용", example = "안녕하세요. 요즘 날씨가 매우 더운데...")
            String content,

            @Schema(description = "알림 발송 시각", example = "3시간 전 또는 2025-08-03 15:20")
            String createdAt
    ) {
        public static NotificationDto of(Notification notification) {
            return new NotificationDto(
                    notification.getNotificationId(),
                    notification.getSender().getName(),
                    notification.getTitle(),
                    shortenContent(notification.getContent()),
                    formatCreatedAt(notification.getCreatedAt())
            );
        }

        private static String shortenContent(String content) {
            return content.length() > 30 ? content.substring(0, 30) + "..." : content;
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


