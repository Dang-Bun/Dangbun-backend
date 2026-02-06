package com.dangbun.domain.cleaning.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;

import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningDetailListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningListResponse;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.GetCleaningUnassignedResponse;
import com.dangbun.domain.cleaning.application.port.in.query.CleaningQuery;
import com.dangbun.domain.duty.original.response.status.DutyExceptionResponse;
import com.dangbun.domain.member.original.response.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckDutyInPlace;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Cleaning", description = "CleaningController - 청소 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}")
public class CleaningQueryController {

    private final CleaningQuery cleaningQuery;

    @Operation(summary = "선택 멤버가 참여 중인 청소의 당번 목록 조회", description = "전달된 memberIds 중 한명이라도 참여한 청소의 당번 목록을 필터링하여 반환합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping("/cleanings/duties")
    public ResponseEntity<BaseResponse<List<GetCleaningListResponse>>> getCleaningList(
            @PathVariable Long placeId,
            @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningQuery.getCleaningList(memberIds)));
    }

    @Operation(summary = "특정 당번의 선택 멤버가 참여 중인 청소 목록 조회", description = "특정 당번 옆의 버튼을 누르면 전달된 memberIds 중 한명이라도 참여한 청소 목록을 필터링하여 반환합니다.")
    @GetMapping("/duties/{dutyId}/cleanings/filter-by-members")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetCleaningDetailListResponse>>> getCleaningDetailList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestParam List<Long> memberIds) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningQuery.getCleaningDetailList(memberIds)));
    }

    @Operation(summary = "미지정 청소 목록 조회", description = " 미지정 청소 항목들의 이름 목록을 반환합니다.")
    @GetMapping("/cleanings/unassigned")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    public ResponseEntity<BaseResponse<List<GetCleaningUnassignedResponse>>> getUnassignedCleanings(
            @PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(cleaningQuery.getUnassignedCleanings()));
    }
}
