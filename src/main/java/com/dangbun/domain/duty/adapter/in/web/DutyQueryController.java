package com.dangbun.domain.duty.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.duty.adapter.in.web.dto.response.GetCleaningInfoListResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.GetDutyCleaningNameListResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.GetDutyListResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.GetDutyMemberNameListResponse;
import com.dangbun.domain.duty.application.port.in.query.*;
import com.dangbun.domain.duty.exception.status.DutyExceptionResponse;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
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
@Tag(name = "Duty", description = "DutyController - 당번 관련 API")
@CheckPlaceMembership
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/duties")
public class DutyQueryController {

    private final DutyQuery dutyQuery;

    @Operation(summary = "당번 목록 조회", description = "해당 플레이스의 당번 목록을 조회합니다.")
    @GetMapping
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    public ResponseEntity<BaseResponse<List<GetDutyListResponse>>> getDutyList(
            @PathVariable Long placeId
    ) {
        DutyListResult result = dutyQuery.getDutyList(placeId);
        List<GetDutyListResponse> response = result.duties().stream()
                .map(GetDutyListResponse::from)
                .toList();
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "당번 정보 - 멤버 이름 목록 조회", description = "당번의 멤버 이름 목록을 조회합니다.")
    @GetMapping("/{dutyId}/members")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetDutyMemberNameListResponse>>> getDutyMemberNameList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId
    ) {
        DutyMembersResult result = dutyQuery.getDutyMembers(dutyId);
        List<GetDutyMemberNameListResponse> response = result.members().stream()
                .map(GetDutyMemberNameListResponse::from)
                .toList();
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "당번 정보 - 청소 이름 목록 조회", description = "당번의 청소 이름 목록을 조회합니다.")
    @GetMapping("/{dutyId}/cleanings")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetDutyCleaningNameListResponse>>> getDutyCleaningNameList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId
    ) {
        DutyCleaningsResult result = dutyQuery.getDutyCleanings(dutyId);
        List<GetDutyCleaningNameListResponse> response = result.cleanings().stream()
                .map(GetDutyCleaningNameListResponse::from)
                .toList();
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "당번 역할 분담 - 청소 목록 조회 (청소 상세 정보 포함)", description = "해당 당번에 해당하는 청소 목록(청소 상세 정보 포함)을 조회합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    @GetMapping("/{dutyId}/cleaning-info")
    public ResponseEntity<BaseResponse<List<GetCleaningInfoListResponse>>> getCleaningInfoList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId
    ) {
        CleaningInfoListResult result = dutyQuery.getCleaningInfoList(dutyId);
        List<GetCleaningInfoListResponse> response = result.cleanings().stream()
                .map(GetCleaningInfoListResponse::from)
                .toList();
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
