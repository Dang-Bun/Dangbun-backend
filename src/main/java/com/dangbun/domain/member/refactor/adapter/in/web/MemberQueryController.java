package com.dangbun.domain.member.refactor.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.member.original.response.status.MemberExceptionResponse;
import com.dangbun.domain.member.refactor.adapter.in.web.dto.response.*;
import com.dangbun.domain.member.refactor.application.port.in.query.MemberQuery;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Validated
@CheckPlaceMembership
@Tag(name = "Member", description = "MemberQueryController - 맴버 조회 API")
@WebAdapter(path = "/places/{placeId}/members")
@RestController
public class MemberQueryController {

    private final MemberQuery memberQuery;

    @Operation(summary = "맴버 목록 조회", description = "플레이스에 참가한 맴버를 조회합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping
    public ResponseEntity<BaseResponse<GetMembersResponse>> getMembers(@PathVariable("placeId") Long placeId) {
        GetMembersResponse response = GetMembersResponse.from(memberQuery.getMembers());
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "대기 맴버 목록 조회", description = "현재 플레이스 참가를 대기하고 있는 맴버들을 조회합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED"}
    )
    @GetMapping("/waiting")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<GetWaitingMembersResponse>> getWaitingMembers(@PathVariable("placeId") Long placeId) {
        GetWaitingMembersResponse response = GetWaitingMembersResponse.from(memberQuery.getWaitingMembers());
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "맴버 정보 조회", description = "한 맴버에 대한 정보를 조회합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBER_NOT_FOUND"}
    )
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<GetMemberDetailResponse>> getMember(@PathVariable("placeId") Long placeId,
                                                                           @PathVariable("memberId") Long memberId) {
        GetMemberDetailResponse response = GetMemberDetailResponse.from(memberQuery.getMember(memberId));
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "내 정보 조회(맴버)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<GetMyInformationResponse>> getMyInformation(@PathVariable("placeId") Long placeId) {
        GetMyInformationResponse response = GetMyInformationResponse.from(memberQuery.getMyInformation());
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    @Operation(summary = "플레이스에 속한 멤버 검색", description = "해당 플레이스에서 이름이 정확히 일치하는 멤버를 검색합니다. (매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED"}
    )
    @GetMapping("/search")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<GetMemberSearchResponse>> searchMemberInPlace(@PathVariable("placeId") Long placeId,
                                                                                     @RequestParam String name) {
        GetMemberSearchResponse response = GetMemberSearchResponse.from(memberQuery.searchByNameInPlace(placeId, name));
        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
