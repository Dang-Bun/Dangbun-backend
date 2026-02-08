package com.dangbun.domain.notification.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.domain.notification.adapter.in.web.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.adapter.in.web.dto.response.PostNotificationCreateResponse;
import com.dangbun.domain.notification.application.port.in.command.NotificationCommandUseCase;
import com.dangbun.domain.notification.response.status.NotificationExceptionResponse;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Validated
@Tag(name = "Notification", description = "NotificationController - 알림함 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/notifications")
public class NotificationCommandController {

    private final NotificationCommandUseCase notificationCommandUseCase;

    @Operation(summary = "알림함 - 알림 작성", description = "작성한 내용의 알림을 전송합니다.")
    @PostMapping
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, NotificationExceptionResponse.class},
            includes = {"MEMBER_NOT_FOUND", "PLACE_ACCESS_DENIED"}
    )
    public ResponseEntity<BaseResponse<PostNotificationCreateResponse>> createNotification(
            @PathVariable Long placeId,
            @Valid @RequestBody PostNotificationCreateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(notificationCommandUseCase.createNotification(request)));
    }
}
