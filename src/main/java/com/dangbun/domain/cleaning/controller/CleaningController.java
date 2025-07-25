package com.dangbun.domain.cleaning.controller;

import com.dangbun.domain.cleaning.dto.response.*;
import com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse;
import com.dangbun.domain.cleaning.service.CleaningService;
import com.dangbun.global.docs.DocumentedApiErrors;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@Tag(name = "Cleaning", description = "CleaningController - 청소 관련 API")
@RestController
@RequiredArgsConstructor
public class CleaningController {

    private final CleaningService cleaningService;

    @Operation(summary = "선택 멤버가 참여 중인 청소의 당번 목록 조회", description = "전달된 memberIds 중 한명이라도 참여한 청소의 당번 목록을 필터링하여 반환합니다.")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping("/cleanings/duties")
    public ResponseEntity<List<GetCleaningListResponse>> getCleaningList(
            @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(cleaningService.getCleaningList(memberIds));
    }

    @Operation(summary = "특정 당번의 선택 멤버가 참여 중인 청소 목록 조회", description = "특정 당번 옆의 버튼을 누르면 전달된 memberIds 중 한명이라도 참여한 청소 목록을 필터링하여 반환합니다.")
    @DocumentedApiErrors(
            value = {CleaningExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @GetMapping("/duties/{dutyId}/cleanings")
    public ResponseEntity<List<GetCleaningDetailListResponse>> getCleaningDetailList(
            @PathVariable Long dutyId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(cleaningService.getCleaningDetailList(dutyId, memberIds));
    }

}
