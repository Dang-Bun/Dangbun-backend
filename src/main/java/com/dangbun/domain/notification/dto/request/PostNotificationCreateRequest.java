package com.dangbun.domain.notification.dto.request;

import com.dangbun.domain.notification.entity.NotificationTemplate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

import static com.dangbun.domain.notification.entity.NotificationTemplate.*;

public record PostNotificationCreateRequest(
        @Schema(description = "알림 받을 멤버 ID 리스트", example = "[1, 2, 3]")
        @NotNull
        List<Long> receiverMemberIds,

        @Schema(description = "알림 템플릿", example = "CLEANING_PENDING")
        @NotNull
        NotificationTemplate template,

        @Schema(description = "알림 내용 (template이 none일 경우에만)", example = "날씨가 굉장히 덥고, 종일 습도는 ... ")
        @Size(max = 1000)
        String content
) {
        @AssertTrue()
        @Schema(hidden = true)
        public boolean isContentFieldsValid() {
                if (template == NONE) {
                        return content != null;
                }
                return true;
        }
}
