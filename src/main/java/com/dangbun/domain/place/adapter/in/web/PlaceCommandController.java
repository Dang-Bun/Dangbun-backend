package com.dangbun.domain.place.adapter.in.web;

import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.domain.place.adapter.in.web.dto.request.DeletePlaceRequest;
import com.dangbun.domain.place.adapter.in.web.dto.request.PatchUpdateTimeRequest;
import com.dangbun.domain.place.adapter.in.web.dto.request.PostRegisterPlaceRequest;
import com.dangbun.domain.place.adapter.in.web.dto.response.PatchUpdateTimeResponse;
import com.dangbun.domain.place.adapter.in.web.dto.response.PostCreateInviteCodeResponse;
import com.dangbun.domain.place.adapter.in.web.dto.response.PostRegisterPlaceResponse;
import com.dangbun.domain.place.exception.status.PlaceExceptionResponse;
import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.place.application.port.in.command.JoinPlaceCommand;
import com.dangbun.domain.place.application.port.in.command.PlaceCommandUseCase;
import com.dangbun.domain.place.application.port.in.command.UpdateTimeCommand;
import com.dangbun.domain.place.application.port.in.command.UpdateTimeResult;
import com.dangbun.domain.user.detail.CustomUserDetails;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Place", description = "PlaceController - 플레이스 관련 API")
@WebAdapter(path = "/places")
public class PlaceCommandController {

    private final PlaceCommandUseCase placeCommandUseCase;

    @Operation(summary = "참여코드 생성", description = "플레이스의 참여코드를 생성합니다.(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED"}
    )
    @PostMapping("/{placeId}/invite-code")
    @CheckPlaceMembership()
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostCreateInviteCodeResponse>> createInviteCode(@PathVariable Long placeId) {

        String inviteCode = placeCommandUseCase.createInviteCode();
        PostCreateInviteCodeResponse data = PostCreateInviteCodeResponse.of(inviteCode);
        return ResponseEntity.ok(BaseResponse.ok(data));
    }


    @Operation(summary = "참여 신청", description = "플레이스에 참가 신청합니다. 플레이스가 요구한 정보들을 입력해야합니다.")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"INVALID_INVITE_CODE", "INVALID_INFORMATION"}
    )
    @PostMapping("/join-requests")
    public ResponseEntity<BaseResponse<PostRegisterPlaceResponse>> registerPlace(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                 @RequestBody PostRegisterPlaceRequest request) {
        JoinPlaceCommand command = JoinPlaceCommand.of(
                userDetails.getUser().getUserId(),
                request.inviteCode(),
                request.name(),
                request.information());
        Long placeId = placeCommandUseCase.joinPlaceRequest(command);
        return ResponseEntity.ok(BaseResponse.ok(PostRegisterPlaceResponse.of(placeId)));
    }


    @Operation(summary = "참여 취소", description = "대기중인 플레이스의 참여 신청을 철회합니다")
    @DeleteMapping("/{placeId}/join-requests")
    @CheckPlaceMembership()
    public ResponseEntity<BaseResponse<?>> deleteRegisterPlace(@PathVariable Long placeId) {
        placeCommandUseCase.cancelRegister();
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @Operation(summary = "플레이스 삭제", description = "플레이스를 삭제합니다(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "INVALID_NAME"}
    )

    @DeleteMapping("/{placeId}")
    @CheckPlaceMembership()
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<?>> deletePlace(@PathVariable Long placeId,
                                                       @RequestBody DeletePlaceRequest request) {
        placeCommandUseCase.deletePlace(request.placeName());
        return ResponseEntity.ok(BaseResponse.ok(null));
    }


    @Operation(summary = "체크리스트 시간 설정", description = "플레이스의 체크리스트 시작시간/종료시간을 설정합니다.(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, PlaceExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE", "INVALID_TIME"}
    )
    @PatchMapping("/{placeId}/settings/time")
    @CheckPlaceMembership()
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PatchUpdateTimeResponse>> updateTime(
            @PathVariable Long placeId,
            @RequestBody PatchUpdateTimeRequest request) {
        UpdateTimeCommand command = UpdateTimeCommand.of(
                request.startTime(),
                request.endTime(),
                request.isToday());
        UpdateTimeResult updateTimeResult = placeCommandUseCase.updateTime(command);

        PatchUpdateTimeResponse response = new PatchUpdateTimeResponse(
                updateTimeResult.startTime(),
                updateTimeResult.endTime(),
                updateTimeResult.isToday());

        return ResponseEntity.ok(BaseResponse.ok(response));
    }
}
