package com.dangbun.domain.notificationreceiver.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.domain.notificationreceiver.adapter.in.web.dto.response.GetNotificationReceivedListResponse;
import com.dangbun.domain.notificationreceiver.application.port.in.query.NotificationReceiverQuery;
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
import org.springframework.web.bind.annotation.PathVariable;

@Validated
@Tag(name = "Notification_Receiver", description = "NotificationReceiverController - 알림함(수신) 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/notifications")
public class NotificationReceiverQueryController {

    private final NotificationReceiverQuery notificationReceiverQuery;

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
        return ResponseEntity.ok(notificationReceiverQuery.getReceivedNotifications(pageable));
    }
}
