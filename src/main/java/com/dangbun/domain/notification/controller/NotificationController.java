package com.dangbun.domain.notification.controller;

import com.dangbun.domain.notification.dto.response.GetMemberSearchListResponse;
import com.dangbun.domain.notification.response.status.NotificationExceptionResponse;
import com.dangbun.domain.notification.service.NotificationService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@Validated
@Tag(name = "Notification", description = "NotificationController - 알림함 관련 API")
@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림함 - 수신인 멤버 검색 결과 조회 api", description = "수신인 선택 화면에서 멤버 검색어 입력 시 이름이 포함된 멤버 결과를 조회합니다.")
    @GetMapping("/notifications/recipients/search")
    @DocumentedApiErrors(
            value = {NotificationExceptionResponse.class},
            includes = {"PLACE_NOT_FOUND", ""}
    )
    public ResponseEntity<BaseResponse<GetMemberSearchListResponse>> searchMembers(
            @RequestParam Long placeId,
            @RequestParam(required = false) @Size(max = 20) String searchname,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(BaseResponse.ok(notificationService.searchMembers(placeId, searchname, pageable)));
    }
}
