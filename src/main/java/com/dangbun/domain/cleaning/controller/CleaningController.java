package com.dangbun.domain.cleaning.controller;

import com.dangbun.domain.cleaning.dto.request.*;
import com.dangbun.domain.cleaning.dto.response.*;
import com.dangbun.domain.cleaning.response.status.CleaningExceptionResponse;
import com.dangbun.domain.cleaning.service.CleaningService;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.response.status.DutyExceptionResponse;
import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckDutyInPlace;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import com.dangbun.global.response.status.BaseExceptionResponse;
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
@CheckPlaceMembership()
@RequestMapping("/places/{placeId}")
@RequiredArgsConstructor
public class CleaningController {

    private final CleaningService cleaningService;

    @Operation(summary = "선택 멤버가 참여 중인 청소의 당번 목록 조회", description = "전달된 memberIds 중 한명이라도 참여한 청소의 당번 목록을 필터링하여 반환합니다.")
    @DocumentedApiErrors(
            value = { MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping("/cleanings/duties")
    public ResponseEntity<BaseResponse<List<GetCleaningListResponse>>> getCleaningList(
            @PathVariable Long placeId,
            @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningService.getCleaningList(memberIds)));
    }

    @Operation(summary = "특정 당번의 선택 멤버가 참여 중인 청소 목록 조회", description = "특정 당번 옆의 버튼을 누르면 전달된 memberIds 중 한명이라도 참여한 청소 목록을 필터링하여 반환합니다.")
    @GetMapping("/duties/{dutyId}/cleanings/filter-by-members")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = { "PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetCleaningDetailListResponse>>> getCleaningDetailList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningService.getCleaningDetailList(memberIds)));
    }

    @Operation(summary = "당번별 청소 생성", description = "입력한 정보들을 바탕으로 새로운 청소를 생성합니다. (매니저용)")
    @PostMapping("/cleanings")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "DUTY_NOT_FOUND", "CLEANING_ALREADY_EXISTS", "INVALID_DATE_FORMAT"}
    )
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostCleaningResponse>> createCleaning(
            @PathVariable Long placeId,
            @RequestBody @Valid PostCleaningCreateRequest request) {

        return ResponseEntity.ok(BaseResponse.ok(cleaningService.createCleaning(request)));
    }

    @Operation(summary = "당번별 청소 수정", description = "입력한 정보들을 바탕으로 청소를 수정합니다. (매니저용)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "CLEANING_NOT_FOUND", "DUTY_NOT_FOUND", "INVALID_DATE_FORMAT", "CLEANING_ALREADY_EXISTS"}
    )
    @PutMapping("/cleanings/{cleaningId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> updateCleaning(
            @PathVariable Long placeId,
            @PathVariable Long cleaningId,
            @Valid @RequestBody PutCleaningUpdateRequest request
    ) {
        cleaningService.updateCleaning(cleaningId, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "청소 삭제", description = "청소 항목을 삭제합니다. (매니저용)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "CLEANING_NOT_FOUND"}
    )
    @DeleteMapping("/cleanings/{cleaningId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> deleteCleaning(
            @PathVariable Long placeId,
            @PathVariable Long cleaningId) {
        cleaningService.deleteCleaning(cleaningId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "미지정 청소 목록 조회", description = " 미지정 청소 항목들의 이름 목록을 반환합니다.")
    @GetMapping("/cleanings/unassigned")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    public ResponseEntity<BaseResponse<List<GetCleaningUnassignedResponse>>> getUnassignedCleanings(
            @PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningService.getUnassignedCleanings()));
    }


}
