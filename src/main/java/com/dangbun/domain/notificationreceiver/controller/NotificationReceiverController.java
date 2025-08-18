package com.dangbun.domain.notificationreceiver.controller;

import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.domain.notificationreceiver.dto.response.GetNotificationReceivedListResponse;
import com.dangbun.domain.notificationreceiver.response.status.NotificationReceiverExceptionResponse;
import com.dangbun.domain.notificationreceiver.service.NotificationReceiverService;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@Tag(name = "Notification_Receiver", description = "NotificationReceiverController - 알림함(수신) 관련 API")
@RestController
@RequestMapping("/places/{placeId}/notifications")
@CheckPlaceMembership()
@RequiredArgsConstructor
public class NotificationReceiverController {

    private final NotificationReceiverService notificationReceiverService;

    @Operation(summary = "받은 알림 목록 조회 (무한스크롤)", description = "현재 로그인한 멤버가 받은 알림들을 무한스크롤 방식으로 조회합니다.")
    @GetMapping("/received")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    public ResponseEntity<GetNotificationReceivedListResponse> getReceivedNotifications(
            @PathVariable Long placeId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(notificationReceiverService.getReceivedNotifications(pageable));
    }

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
        notificationReceiverService.markAsRead(notificationId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
