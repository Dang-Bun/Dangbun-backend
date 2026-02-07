package com.dangbun.domain.notificationreceiver.refactor.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.domain.notificationreceiver.refactor.application.port.in.command.NotificationReceiverCommandUseCase;
import com.dangbun.domain.notificationreceiver.refactor.response.status.NotificationReceiverExceptionResponse;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Validated
@Tag(name = "Notification_Receiver", description = "NotificationReceiverController - 알림함(수신) 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/notifications")
public class NotificationReceiverCommandController {

    private final NotificationReceiverCommandUseCase notificationReceiverCommandUseCase;

    @Operation(summary = "받은 알림 읽음 처리", description = "특정 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{notificationId}/read")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, NotificationReceiverExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "NOTIFICATION_RECEIVER_NOT_FOUND"}
    )
    public ResponseEntity<BaseResponse<Void>> markAsRead(
            @PathVariable Long placeId,
            @PathVariable Long notificationId
    ) {
        notificationReceiverCommandUseCase.markAsRead(notificationId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
