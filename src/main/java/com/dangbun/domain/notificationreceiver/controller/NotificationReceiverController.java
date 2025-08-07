package com.dangbun.domain.notificationreceiver.controller;

import com.dangbun.domain.notification.response.status.NotificationExceptionResponse;
import com.dangbun.domain.notificationreceiver.dto.response.GetNotificationReceivedListResponse;
import com.dangbun.domain.notificationreceiver.service.NotificationReceiverService;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Validated
@Tag(name = "Notification_Receiver", description = "NotificationReceiverController - 알림함(수신) 관련 API")
@RestController
@CheckPlaceMembership(placeIdParam = "placeId")
@RequiredArgsConstructor
public class NotificationReceiverController {

    private final NotificationReceiverService notificationReceiverService;

    @Operation(summary = "받은 알림 목록 조회 (무한스크롤)", description = "현재 로그인한 멤버가 받은 알림들을 무한스크롤 방식으로 조회합니다.")
    @GetMapping("notifications/received")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {""}
    )
    public ResponseEntity<GetNotificationReceivedListResponse> getReceivedNotifications(
            @RequestParam Long placeId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(notificationReceiverService.getReceivedNotifications(pageable));
    }


}
