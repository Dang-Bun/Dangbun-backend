package com.dangbun.domain.member.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.domain.member.adapter.in.web.dto.request.ExitPlaceRequest;
import com.dangbun.domain.member.adapter.in.web.dto.request.RemoveMemberRequest;
import com.dangbun.domain.member.application.port.in.command.AssignDutyCommand;
import com.dangbun.domain.member.application.port.in.command.MemberCommandUseCase;
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
@Tag(name = "Member", description = "MemberCommandController - 맴버 명령 API")
@WebAdapter(path = "/places/{placeId}/members")
@RestController
public class MemberCommandController {

    private final MemberCommandUseCase memberCommandUseCase;

    @Operation(summary = "맴버 수락", description = "대기중인 맴버의 참가를 수락합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED", "MEMBER_NOT_FOUND"}
    )
    @PostMapping("/{memberId}/accept")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> registerMember(@PathVariable("placeId") Long placeId,
                                                             @PathVariable("memberId") Long memberId) {
        memberCommandUseCase.registerMember(memberId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "맴버 거절", description = "대기중인 맴버의 참가를 거절합니다.(매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED", "MEMBER_NOT_FOUND"}
    )
    @DeleteMapping("/waiting/{memberId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> removeWaitingMember(@PathVariable("placeId") Long placeId,
                                                                  @PathVariable("memberId") Long memberId) {
        memberCommandUseCase.removeWaitingMember(memberId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "플레이스 나가기", description = "플레이스에서 나갑니다. (멤버용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "PLACE_NAME_NOT_MATCHED", "INVALID_ROLE"}
    )
    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<Void>> exitPlace(@PathVariable("placeId") Long placeId,
                                                        @RequestBody ExitPlaceRequest request) {
        memberCommandUseCase.exitPlace(request.toCommand());
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "맴버 추방", description = "현재 플레이스에 속해있는 맴버를 추방합니다. (매니저용)")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED", "NO_SUCH_USER", "NAME_NOT_MATCHED"}
    )
    @DeleteMapping("/{memberId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> removeMember(@PathVariable("placeId") Long placeId,
                                                           @PathVariable("memberId") Long memberId,
                                                           @RequestBody RemoveMemberRequest request) {
        memberCommandUseCase.removeMember(request.toCommand(memberId));
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "멤버 관리 - 당번 설정")
    @DocumentedApiErrors(
            value = MemberExceptionResponse.class,
            includes = {"PLACE_ACCESS_DENIED"}
    )
    @PostMapping("/{memberId}/duties/{dutyId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> assignDutyToMember(@PathVariable Long memberId,
                                                                 @PathVariable Long dutyId) {
        memberCommandUseCase.assignDutyToMember(new AssignDutyCommand(memberId, dutyId));
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
