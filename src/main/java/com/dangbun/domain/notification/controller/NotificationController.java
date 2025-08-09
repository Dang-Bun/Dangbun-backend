package com.dangbun.domain.notification.controller;

import com.dangbun.domain.notification.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.dto.response.*;
import com.dangbun.domain.notification.response.status.NotificationExceptionResponse;
import com.dangbun.domain.notification.service.NotificationService;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@Tag(name = "Notification", description = "NotificationController - 알림함 관련 API")
@CheckPlaceMembership()
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림함 - 수신인 멤버 검색 결과 조회 (무한 스크롤)", description = "수신인 선택 화면에서 멤버 검색어 입력 시 이름이 포함된 멤버 결과를 조회합니다.")
    @GetMapping("/recipients/search")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {}
    )
    public ResponseEntity<BaseResponse<GetMemberSearchListResponse>> searchMembers(
            @RequestParam Long placeId,
            @RequestParam(required = false) @Size(max = 20) String searchname,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.ok(notificationService.searchMembers(searchname, pageable)));
    }

    @Operation(summary = "알림함 - 수신인(멤버 검색) 최근 검색어 조회", description = "수신인 선택 화면에서 최근 검색한 멤버 이름을 최대 5개까지 조회합니다.")
    @GetMapping("/recipients/recent-searches")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {}
    )
    public ResponseEntity<BaseResponse<GetRecentSearchResponse>> getRecentSearches(
            @RequestParam Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(notificationService.getRecentSearches()));
    }

    @Operation(summary = "알림함 - 알림 작성", description = "작성한 내용의 알림을 전송합니다.")
    @PostMapping
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {"MEMBER_NOT_FOUND"}
    )
    public ResponseEntity<BaseResponse<PostNotificationCreateResponse>> createNotification(
            @RequestParam Long placeId,
            @Valid @RequestBody PostNotificationCreateRequest request
    ) {

        return ResponseEntity.ok(BaseResponse.ok(notificationService.createNotification(request)));
    }

    @Operation(summary = "보낸 알림 목록 조회 (무한스크롤)", description = "현재 로그인한 멤버가 보낸 알림들을 무한스크롤 방식으로 조회합니다.")
    @GetMapping("/sent")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {""}
    )
    public ResponseEntity<BaseResponse<GetNotificationListResponse>> getNotifications(
            @RequestParam Long placeId,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.ok(notificationService.getNotificationList(pageable)));
    }

    @GetMapping("/{notificationId}")
    @Operation(summary = "알림 상세 조회", description = "알림함에서 특정 알림을 클릭했을 때 상세 내용을 조회합니다.")
    @DocumentedApiErrors(
            value = NotificationExceptionResponse.class,
            includes = {"NOTIFICATION_NOT_FOUND", "NOTIFICATION_ACCESS_FORBIDDEN"}
    )
    public ResponseEntity<GetNotificationInfoResponse> getNotificationInfo(
            @RequestParam Long placeId,
            @PathVariable Long notificationId
    ) {
        return ResponseEntity.ok(notificationService.getNotificationInfo(notificationId));
    }

}
