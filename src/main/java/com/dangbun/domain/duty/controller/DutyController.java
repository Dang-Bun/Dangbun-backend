package com.dangbun.domain.duty.controller;

import com.dangbun.domain.duty.dto.request.*;
import com.dangbun.domain.duty.dto.response.*;
import com.dangbun.domain.duty.response.status.DutyExceptionResponse;
import com.dangbun.domain.duty.service.DutyService;
import com.dangbun.global.aop.CheckDutyInPlace;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
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
@Tag(name = "Duty", description = "DutyController - 당번 관련 API")
@RestController
@CheckPlaceMembership
@RequestMapping("/places/{placeId}/duties")
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;

    @Operation(summary = "당번 생성", description = "플레이스에 새로운 당번을 생성합니다. (매니저용)")
    @PostMapping
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"PLACE_NOT_FOUND", "DUTY_ALREADY_EXISTS"}
    )
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostDutyCreateResponse>> createDuty(
            @PathVariable Long placeId,
            @RequestBody @Valid PostDutyCreateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.createDuty(request)));
    }

    @Operation(summary = "당번 목록 조회", description = "해당 플레이스의 당번 목록을 조회합니다.")
    @GetMapping
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"PLACE_NOT_FOUND"}
    )
    public ResponseEntity<BaseResponse<List<GetDutyListResponse>>> getDutyList(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.getDutyList()));
    }

    @Operation(summary = "당번 수정", description = "해당 당번의 이름이나 아이콘을 수정합니다. (매니저용)")
    @PutMapping("/{dutyId}")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PutDutyUpdateResponse>> updateDuty(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody @Valid PutDutyUpdateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.updateDuty( request)));
    }

    @Operation(summary = "당번 삭제", description = "해당 당번을 삭제합니다. (매니저용)")
    @DeleteMapping("/{dutyId}")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> deleteDuty(
            @PathVariable Long placeId,
            @PathVariable Long dutyId) {
        dutyService.deleteDuty(dutyId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번 정보 - 멤버 이름 목록 조회", description = "당번의 멤버 이름 목록을 조회합니다.")
    @GetMapping("/{dutyId}/members")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetDutyMemberNameListResponse>>> getDutyMemberNameList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.getDutyMemberNameList()));
    }

    @Operation(summary = "당번 정보 - 청소 이름 목록 조회", description = "당번의 청소 이름 목록을 조회합니다.")
    @GetMapping("/{dutyId}/cleanings")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    public ResponseEntity<BaseResponse<List<GetDutyCleaningNameListResponse>>> getDutyCleaningNameList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.getDutyCleaningNameList()));
    }

    @Operation(summary = "당번 정보 - 멤버 추가", description = "당번에 멤버를 추가합니다. (매니저용)")
    @PostMapping("/{dutyId}/members")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND", "MEMBER_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostAddMembersResponse>> addMembers(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody PostAddMembersRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.addMembers(request)));
    }

    @Operation(summary = "당번 역할 분담 (공통/랜덤/직접)", description = "해당 당번에 해당하는 청소에 멤버를 지정합니다. (매니저용)")
    @PatchMapping("/{dutyId}/cleanings/members")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND", "CLEANING_NOT_FOUND", "MEMBER_NOT_EXISTS"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> assignMember(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody @Valid PatchAssignMemberRequest request
    ) {
        dutyService.assignMember(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번 역할 분담 - 청소 목록 조회 (청소 상세 정보 포함)", description = "해당 당번에 해당하는 청소 목록(청소 상세 정보 포함)을 조회합니다.")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @GetMapping("/{dutyId}/cleaning-info")
    public ResponseEntity<BaseResponse<List<GetCleaningInfoListResponse>>> getCleaningInfoList(
            @PathVariable Long placeId,
            @PathVariable Long dutyId) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.getCleaningInfoList()));
    }


    @Operation(summary = "당번 정보 - 미지정 청소 추가", description = "당번에 미지정 청소를 추가합니다. (매니저용)")
    @PostMapping("/{dutyId}/cleanings")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"DUTY_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostAddCleaningsResponse>> addCleanings(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody PostAddCleaningsRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.addCleanings( request)));
    }

    @Operation(summary = "당번에서 청소 항목 제거", description = "지정된 당번에서 특정 청소 항목을 제거하면 해당 청소는 미지정 상태로 되돌아갑니다. (매니저용)")
    @DeleteMapping("/{dutyId}/cleanings/{cleaningId}")
    @DocumentedApiErrors(
            value = DutyExceptionResponse.class,
            includes = {"DUTY_NOT_FOUND", "CLEANING_NOT_FOUND", "CLEANING_NOT_ASSIGNED"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> removeCleaning(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @PathVariable Long cleaningId) {
        dutyService.removeCleaningFromDuty(cleaningId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

}
