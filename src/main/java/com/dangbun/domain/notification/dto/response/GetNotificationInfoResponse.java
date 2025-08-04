package com.dangbun.domain.notification.dto.response;

import com.dangbun.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record GetNotificationInfoResponse(
        @Schema(description = "알림 ID", example = "1")
        Long notificationId,

        @Schema(description = "보낸 사람 이름", example = "지윤")
        String senderName,

        @Schema(description = "받은 사람 이름 목록", example = "[\"멤버1\", \"멤버2\"]")
        List<String> receiverNames,

        @Schema(description = "알림 생성 시각", example = "2025-08-04T21:45:00")
        LocalDateTime createdAt,

        @Schema(description = "알림 제목", example = "청소 일정 변경 안내")
        String title,

        @Schema(description = "알림 내용", example = "내일 예정된 화장실 청소는 다음 주로 연기됩니다.")
        String content
) {
    public static GetNotificationInfoResponse of(Notification notification, List<String> receiverNames) {
        return new GetNotificationInfoResponse(
                notification.getNotificationId(),
                notification.getSender().getName(),
                receiverNames,
                notification.getCreatedAt(),
                notification.getTitle(),
                notification.getContent());
    }
}
