package com.dangbun.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostNotificationCreateResponse (
        @Schema(description = "전송 완료한 알림 id", example = "1")
        Long notificationId
) {
    public static PostNotificationCreateResponse of(Long notificationId) {
        return new PostNotificationCreateResponse(notificationId);
    }
}
