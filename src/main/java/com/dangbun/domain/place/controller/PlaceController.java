package com.dangbun.domain.place.controller;

import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.domain.place.dto.request.PatchUpdateTimeRequest;
import com.dangbun.domain.place.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.dto.request.PostCreatePlaceRequest;
import com.dangbun.domain.place.dto.request.PostRegisterPlaceRequest;
import com.dangbun.domain.place.dto.response.*;
import com.dangbun.domain.place.response.status.PlaceExceptionResponse;
import com.dangbun.domain.place.service.PlaceService;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.response.status.UserExceptionResponse;
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

import java.util.List;

@RestController
@Validated
@RequestMapping("/places")
@Tag(name = "Place", description = "PlaceController - 플레이스 관련 API")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = " 플레이스 목록 조회", description = "사용자의 모든 플레이스를 조회하기 위해 사용됩니다(플레이스 선택 화면)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping()
    public ResponseEntity<BaseResponse<GetPlaceListResponse>> getPlaces(@AuthenticationPrincipal(expression = "user") User user) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getPlaces(user.getUserId())));
    }

    @Operation(summary = "플레이스 생성", description = "플레이스를 생성합니다. 플레이스를 생성한 user는 매니저가 됩니다.")
    @DocumentedApiErrors(
            value = {UserExceptionResponse.class},
            includes = {"NO_SUCH_USER"}
    )
    @PostMapping
    public ResponseEntity<BaseResponse<PostCreatePlaceResponse>> createPlace(@AuthenticationPrincipal(expression = "user") User user,
                                                       @RequestBody PostCreatePlaceRequest request) {


        return ResponseEntity.ok(BaseResponse.ok(placeService.createPlaceWithManager(user.getUserId(), request)));
    }

    @Operation(summary = "참여코드 생성", description = "플레이스의 참여코드를 생성합니다.(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"INVALID_ROLE", "NO_SUCH_MEMBER"}
    )
    @PostMapping("/{placeId}/invite-code")
    @CheckPlaceMembership(placeIdParam = "placeId")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostCreateInviteCodeResponse>> createInviteCode(@PathVariable Long placeId) {
        PostCreateInviteCodeResponse data = placeService.createInviteCode();
        return ResponseEntity.ok(BaseResponse.ok(data));
    }

    @Operation(summary = "참여코드 확인", description = "참여코드를 입력합니다. 성공적으로 입력할 시 정보 입력 창이 뜹니다.")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"ALREADY_INVITED", "NO_SUCH_INVITE"}
    )
    @PostMapping("/invite-code")
    public ResponseEntity<BaseResponse<PostCheckInviteCodeResponse>> checkInviteCode(@AuthenticationPrincipal(expression = "user") User user,
                                                                                     @RequestBody PostCheckInviteCodeRequest request) {

        PostCheckInviteCodeResponse response = placeService.checkInviteCode(user, request);
        return ResponseEntity.ok(BaseResponse.ok(response));

    }

    @Operation(summary = "참여 신청", description = "플레이스에 참가 신청합니다. 플레이스가 요구한 정보들을 입력해야합니다.")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"NO_SUCH_INVITE", "INVALID_INFORMATION"}
    )
    @PostMapping("/join-requests")
    public ResponseEntity<BaseResponse<PostRegisterPlaceResponse>> registerPlace(@AuthenticationPrincipal(expression = "user") User user,
                                                                                 @RequestBody PostRegisterPlaceRequest request) {

        return ResponseEntity.ok(BaseResponse.ok(placeService.joinRequest(user, request)));
    }

    @Operation(summary = "참여 취소",description = "대기중인 플레이스의 참여 신청을 철회합니다")
    @DeleteMapping("/{placeId}/join-requests")
    @CheckPlaceMembership(placeIdParam = "placeId")
    public ResponseEntity<BaseResponse<?>> deleteRegisterPlace(@PathVariable Long placeId){
        placeService.cancelRegister();
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "플레이스 조회", description = "플레이스를 조회합니다(홈화면)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"NO_SUCH_MEMBER"}
    )
    @CheckPlaceMembership(placeIdParam = "placeId")
    @GetMapping("/{placeId}")
    public ResponseEntity<BaseResponse<GetPlaceResponse>> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getPlace()));
    }

    @Operation(summary = "플레이스 삭제", description = "플레이스를 삭제합니다(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"NO_SUCH_MEMBER", "INVALID_ROLE"}
    )
    @DeleteMapping("/{placeId}")
    @CheckPlaceMembership(placeIdParam = "placeId")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<?>> deletePlace(@PathVariable Long placeId) {
        placeService.deletePlace();
        return ResponseEntity.ok(BaseResponse.ok(null));
    }



    @Operation(summary = "체크리스트 시간 설정", description = "플레이스의 체크리스트 시작시간/종료시간을 설정합니다.(매니저)")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"INVALID_TIME"}
    )
    @PatchMapping("/{placeId}/settings/time")
    @CheckPlaceMembership(placeIdParam = "placeId")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<?>> updateTime(
                                                      @PathVariable Long placeId,
                                                      @RequestBody PatchUpdateTimeRequest request){
        placeService.updateTime(request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "매니저-전체 진행률", description = "플레이스 내의 모든 당번에 대한 진행률을 보여줍니다.")
    @DocumentedApiErrors(
            value = {},
            includes = {}
    )
    @CheckPlaceMembership(placeIdParam = "placeId")
    @CheckManagerAuthority
    @GetMapping("/{placeId}/duties/progress")
    public ResponseEntity<BaseResponse<GetDutiesProgressResponse>> getDutiesProgress(@PathVariable Long placeId){
        return ResponseEntity.ok(BaseResponse.ok(placeService.getDutiesProgress()));
    }
}
