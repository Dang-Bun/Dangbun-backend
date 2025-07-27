package com.dangbun.domain.cleaning.controller;

import com.dangbun.domain.cleaning.dto.request.*;
import com.dangbun.domain.cleaning.dto.response.*;
import com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse;
import com.dangbun.domain.cleaning.service.CleaningService;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    @GetMapping("/duties/{dutyId}/cleanings")
    @DocumentedApiErrors(
            value = {CleaningExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    public ResponseEntity<List<GetCleaningDetailListResponse>> getCleaningDetailList(
            @PathVariable Long dutyId, @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(cleaningService.getCleaningDetailList(dutyId, memberIds));
    }

    @Operation(summary = "당번별 청소 생성", description = "입력한 정보들을 바탕으로 새로운 청소를 생성합니다.")
    @PostMapping("/cleanings")
    @DocumentedApiErrors(
            value = {CleaningExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND", "INVALID_DATE_FORMAT", "CLEANING_ALREADY_EXISTS"}
    )
    public ResponseEntity<BaseResponse<PostCleaningResponse>> createCleaning(@RequestBody @Valid PostCleaningCreateRequest request) {

        return ResponseEntity.ok(BaseResponse.ok(cleaningService.createCleaning(request)));
    }

    @Operation(summary = "당번별 청소 수정", description = "입력한 정보들을 바탕으로 청소를 수정합니다.")
    @DocumentedApiErrors(
            value = {CleaningExceptionResponse.class},
            includes = {"CLEANING_NOT_FOUND", "DUTY_NOT_FOUND", "INVALID_DATE_FORMAT", "CLEANING_ALREADY_EXISTS"}
    )
    @PutMapping("/cleanings/{cleaningId}")
    public ResponseEntity<BaseResponse<Void>> updateCleaning(
            @PathVariable Long cleaningId,
            @Valid @RequestBody PutCleaningUpdateRequest request
    ) {
        cleaningService.updateCleaning(cleaningId, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }



}
