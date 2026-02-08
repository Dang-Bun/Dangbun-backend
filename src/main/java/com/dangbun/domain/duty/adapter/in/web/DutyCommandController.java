package com.dangbun.domain.duty.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.duty.adapter.in.web.dto.request.*;
import com.dangbun.domain.duty.adapter.in.web.dto.response.PostAddCleaningsResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.PostDutyCreateResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.PutAddMembersResponse;
import com.dangbun.domain.duty.adapter.in.web.dto.response.PutDutyUpdateResponse;
import com.dangbun.domain.duty.application.port.in.command.*;
import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import com.dangbun.domain.duty.application.port.in.query.AddMembersResult;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.exception.status.DutyExceptionResponse;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
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

@Validated
@Tag(name = "Duty", description = "DutyController - 당번 관련 API")
@CheckPlaceMembership
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/duties")
public class DutyCommandController {

    private final DutyCommandUseCase dutyCommandUseCase;

    @Operation(summary = "당번 생성", description = "플레이스에 새로운 당번을 생성합니다. (매니저용)")
    @PostMapping
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "DUTY_ALREADY_EXISTS"}
    )
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostDutyCreateResponse>> createDuty(
            @PathVariable Long placeId,
            @RequestBody @Valid PostDutyCreateRequest request
    ) {
        CreateDutyCommand command = new CreateDutyCommand(
                placeId,
                request.name(),
                request.icon()
        );
        Long dutyId = dutyCommandUseCase.createDuty(command);
        return ResponseEntity.ok(BaseResponse.ok(PostDutyCreateResponse.of(dutyId)));
    }

    @Operation(summary = "당번 수정", description = "해당 당번의 이름이나 아이콘을 수정합니다. (매니저용)")
    @PutMapping("/{dutyId}")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PutDutyUpdateResponse>> updateDuty(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody @Valid PutDutyUpdateRequest request
    ) {
        UpdateDutyCommand command = new UpdateDutyCommand(
                dutyId,
                request.name(),
                request.icon()
        );
        UpdateDutyResult result = dutyCommandUseCase.updateDuty(command);
        return ResponseEntity.ok(BaseResponse.ok(PutDutyUpdateResponse.from(result)));
    }

    @Operation(summary = "당번 삭제", description = "해당 당번을 삭제합니다. (매니저용)")
    @DeleteMapping("/{dutyId}")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "DUTY_NOT_IN_PLACE"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> deleteDuty(
            @PathVariable Long placeId,
            @PathVariable Long dutyId
    ) {
        dutyCommandUseCase.deleteDuty(dutyId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번 정보 - 멤버 수정", description = "당번의 멤버를 수정합니다. (매니저용)")
    @PutMapping("/{dutyId}/members")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "MEMBER_NOT_FOUND"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PutAddMembersResponse>> putAddMembers(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody PutAddMembersRequest request
    ) {
        AddMembersCommand command = new AddMembersCommand(
                dutyId,
                request.memberIds()
        );
        AddMembersResult result = dutyCommandUseCase.addMembers(command);
        return ResponseEntity.ok(BaseResponse.ok(PutAddMembersResponse.from(result)));
    }

    @Operation(summary = "당번 역할 분담 (공통/랜덤/직접)", description = "해당 당번에 해당하는 청소에 멤버를 지정합니다. (매니저용)")
    @PatchMapping("/{dutyId}/cleanings/members")
    @DocumentedApiErrors(
            value = {DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "CLEANING_NOT_FOUND", "MEMBER_NOT_EXISTS"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> assignMember(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody @Valid PatchAssignMemberRequest request
    ) {
        AssignMemberCommand command = new AssignMemberCommand(
                dutyId,
                request.assignType(),
                request.cleaningId(),
                request.memberIds(),
                request.assignCount()
        );
        dutyCommandUseCase.assignMember(command);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번 정보 - 미지정 청소 추가", description = "당번에 미지정 청소를 추가합니다. (매니저용)")
    @PostMapping("/{dutyId}/cleanings")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostAddCleaningsResponse>> addCleanings(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @RequestBody PostAddCleaningsRequest request
    ) {
        AddCleaningsCommand command = new AddCleaningsCommand(
                dutyId,
                request.cleaningIds()
        );
        AddCleaningsResult result = dutyCommandUseCase.addCleanings(command);
        return ResponseEntity.ok(BaseResponse.ok(PostAddCleaningsResponse.from(result)));
    }

    @Operation(summary = "당번에서 청소 항목 제거", description = "지정된 당번에서 특정 청소 항목을 제거하면 해당 청소는 미지정 상태로 되돌아갑니다. (매니저용)")
    @DeleteMapping("/{dutyId}/cleanings/{cleaningId}")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, DutyExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "DUTY_NOT_IN_PLACE", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "CLEANING_NOT_FOUND", "CLEANING_NOT_ASSIGNED"}
    )
    @CheckDutyInPlace
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> removeCleaning(
            @PathVariable Long placeId,
            @PathVariable Long dutyId,
            @PathVariable Long cleaningId
    ) {
        RemoveCleaningCommand command = new RemoveCleaningCommand(dutyId, cleaningId);
        dutyCommandUseCase.removeCleaning(command);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
