package com.dangbun.domain.notification.controller;

import com.dangbun.domain.notification.dto.request.PostNotificationCreateRequest;
import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.dto.response.GetRecentSearchResponse;
import com.dangbun.domain.notification.dto.response.PostNotificationCreateResponse;
import com.dangbun.domain.notification.response.status.NotificationExceptionResponse;
import com.dangbun.domain.notification.service.NotificationService;
import com.dangbun.global.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Validated
@Tag(name = "Notification", description = "NotificationController - 알림함 관련 API")
@CheckPlaceMembership(placeIdParam = "placeId")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림함 - 수신인 멤버 검색 결과 조회", description = "수신인 선택 화면에서 멤버 검색어 입력 시 이름이 포함된 멤버 결과를 조회합니다.")
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
        return ResponseEntity.ok(BaseResponse.ok(notificationService.searchMembers(placeId, searchname, pageable)));
    }

    @Operation(summary = "알림함 - 수신인(멤버 검색) 최근 검색어 조회", description = "수신인 선택 화면에서 최근 검색한 멤버 이름을 최대 5개까지 조회합니다.")
    @GetMapping("/recipients/recent-searches")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {}
    )
    public ResponseEntity<BaseResponse<GetRecentSearchResponse>> getRecentSearches(
            @RequestParam Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(notificationService.getRecentSearches(placeId)));
    }

    @Operation(summary = "알림함 - 알림 작성", description = "작성한 내용의 알림을 전송합니다.")
    @PostMapping
    public ResponseEntity<BaseResponse<PostNotificationCreateResponse>> createNotification(
            @RequestParam Long placeId,
            @Valid @RequestBody PostNotificationCreateRequest request
    ) {

        return ResponseEntity.ok(BaseResponse.ok(notificationService.createNotification(request)));
    }


}
