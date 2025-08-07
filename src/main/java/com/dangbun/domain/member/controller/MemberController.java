package com.dangbun.domain.member.controller;

import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.domain.member.dto.request.DeleteMemberRequest;
import com.dangbun.domain.member.dto.request.DeleteSelfFromPlaceRequest;
import com.dangbun.domain.member.dto.response.*;
import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.domain.member.service.MemberService;
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
@CheckPlaceMembership(placeIdParam = "placeId")
@Tag(name = "Member", description = "MemberController - 맴버 관련 API")
@RequestMapping("/places/{placeId}/members")
@RestController
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "맴버 목록 조회", description = "플레이스에 참가한 맴버를 조회합니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping
    public ResponseEntity<BaseResponse<GetMembersResponse>> getMembers(@PathVariable("placeId") Long placeId) {
        GetMembersResponse response = memberService.getMembers(placeId);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }


    @Operation(summary = "대기 맴버 목록 조회", description = "현재 플레이스 참가를 대기하고 있는 맴버들을 조회합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","INVALID_ROLE"}
    )
    @GetMapping("/waiting")
    public ResponseEntity<BaseResponse<GetWaitingMembersResponse>> getWaitingMembers(@PathVariable("placeId") Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(memberService.getWaitingMembers(placeId)));
    }

    @Operation(summary = "맴버 수락", description = "대기중인 맴버의 참가를 수락합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","INVALID_ROLE","NO_SUCH_MEMBER"}
    )
    @PostMapping("/{memberId}/accept")
    public ResponseEntity<?> registerMember(@PathVariable("placeId") Long placeId,
                                            @PathVariable("memberId") Long memberId) {
        memberService.registerMember(placeId, memberId);

        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "맴버 거절", description = "대기중인 맴버의 참가를 거절합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","INVALID_ROLE","NO_SUCH_MEMBER"}
    )
    @DeleteMapping("/waiting/{memberId}")
    public ResponseEntity<?> removeWaitingMember(@PathVariable("placeId") Long placeId,
                                                 @PathVariable("memberId") Long memberId) {

        memberService.removeWaitingMember(placeId, memberId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "맴버 정보 조회", description = "한 맴버에 대한 정보를 조회합니다.")
    @DocumentedApiErrors(
            value = {},
            includes = {"PLACE_ACCESS_DENIED","NO_SUCH_MEMBER"}
    )
    @GetMapping("/{memberId}")
    public ResponseEntity<BaseResponse<GetMemberResponse>> getMember(@PathVariable("placeId") Long placeId,
                                                                     @PathVariable("memberId") Long memberId) {

        return ResponseEntity.ok(BaseResponse.ok(memberService.getMember(placeId, memberId)));
    }

    @Operation(summary = "플레이스 나가기", description = "플레이스에서 나갑니다")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","PLACE_NAME_NOT_MATCHED", "INVALID_ROLE"}
    )
    @DeleteMapping("/me")
    public ResponseEntity<?> removeSelfFromPlace(@PathVariable("placeId") Long placeId,
                                                 @RequestBody DeleteSelfFromPlaceRequest request) {
        memberService.exitPlace(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "맴버 추방", description = "현재 플레이스에 속해있는 맴버를 추방합니다")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","INVALID_ROLE", "NO_SUCH_USER", "NAME_NOT_MATCHED"}
    )
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable("placeId") Long placeId,
                                          @PathVariable("memberId") Long memberId,
                                          @RequestBody DeleteMemberRequest request) {
        memberService.removeMember(memberId, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "당번에 멤버 추가 - 플레이스에 속한 멤버 검색", description = "해당 플레이스에서 이름이 정확히 일치하는 멤버를 검색합니다.")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED","INVALID_ROLE"}
    )
    @GetMapping("/search")
    public ResponseEntity<BaseResponse<GetMemberSearchResponse>> searchMemberInPlace(@PathVariable("placeId") Long placeId,
                                                                                     @RequestParam String name) {
        return ResponseEntity.ok(BaseResponse.ok(memberService.searchByNameInPlace(placeId, name)));
    }


    @Operation(summary = "내 정보 조회(맴버)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @GetMapping("/me")
    public ResponseEntity<BaseResponse<GetMyInformationResponse>> getMyInformation(@PathVariable("placeId") Long placeId){
        return ResponseEntity.ok(BaseResponse.ok(memberService.getMyInformation(placeId)));
    }

}
