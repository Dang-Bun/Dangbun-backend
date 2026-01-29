package com.dangbun.domain.place.refactor.adapter.in.web;

import com.dangbun.domain.member.response.status.MemberExceptionResponse;
import com.dangbun.domain.place.original.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.original.dto.response.*;
import com.dangbun.domain.place.original.response.status.PlaceExceptionResponse;
import com.dangbun.domain.place.refactor.WebAdapter;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceQuery;
import com.dangbun.domain.place.refactor.domain.Place;
import com.dangbun.domain.user.entity.CustomUserDetails;
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

import java.util.ArrayList;
import java.util.List;


@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Place", description = "PlaceController - 플레이스 관련 API")
@WebAdapter("/places")
public class PlaceQueryController {

    private final PlaceQuery placeQuery;

    @Operation(summary = " 플레이스 목록 조회", description = "사용자의 모든 플레이스를 조회하기 위해 사용됩니다(플레이스 선택 화면)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping()
    public ResponseEntity<BaseResponse<GetPlaceListResponse>> getPlaces(@AuthenticationPrincipal CustomUserDetails userDetails) {

        List<Place> placeList = placeQuery.getPlaceList(userDetails.getUser().getUserId());
        for (Place place : placeList) {
            ArrayList<GetPlaceListResponse.PlaceDto> placeDto = GetPlaceListResponse.PlaceDto.of(
                    place.getPlaceId(),
                    place.getName(),
                    place.getCategory(),
                    place.getCategoryName(),
                    place,
                    place.e
            )
        }

        return ResponseEntity.ok(BaseResponse.ok(placeService.getPlaces(userDetails.getUser().getUserId())));

    }


    @Operation(summary = "참여코드 확인", description = "참여코드를 입력합니다. 성공적으로 입력할 시 정보 입력 창이 뜹니다.")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"ALREADY_INVITED", "INVALID_INVITE_CODE"}
    )
    @PostMapping("/invite-code")
    public ResponseEntity<BaseResponse<PostCheckInviteCodeResponse>> checkInviteCode(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                     @RequestBody PostCheckInviteCodeRequest request) {

        PostCheckInviteCodeResponse response = placeService.checkInviteCode(userDetails.getUser(), request);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }


    @Operation(summary = "플레이스 조회", description = "플레이스를 조회합니다(홈화면)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @CheckPlaceMembership()
    @GetMapping("/{placeId}")
    public ResponseEntity<BaseResponse<GetPlaceResponse>> getPlace(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getPlace()));
    }


    @Operation(summary = "체크리스트 시간 조회", description = "플레이스의 체크리스트 시작시간/종료시간 및 isToday를 조회합니다.(매니저)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping("/{placeId}/settings/time")
    @CheckPlaceMembership()
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<GetTimeResponse>> getTime(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getTimeAndIsToday()));
    }


    @Operation(summary = "매니저-전체 진행률", description = "플레이스 내의 모든 당번에 대한 진행률을 보여줍니다.")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "MEMBERSHIP_UNAUTHORIZED", "INVALID_ROLE"}
    )
    @CheckPlaceMembership()
    @CheckManagerAuthority
    @GetMapping("/{placeId}/duties/progress")
    public ResponseEntity<BaseResponse<GetDutiesProgressResponse>> getDutiesProgress(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getDutiesProgress()));
    }


    @Operation(summary = "참여코드 조회", description = "플레이스의 참여코드를 조회합니다.(매니저)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, PlaceExceptionResponse.class},
            includes = {"PLACE_ACCESS_DENIED", "INVALID_ROLE", "MEMBERSHIP_UNAUTHORIZED", "INVITE_CODE_NOT_EXISTS"}
    )
    @GetMapping("/{placeId}/invite-code")
    @CheckPlaceMembership()
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<GetPlaceInvitedCodeResponse>> getInviteCode(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(placeService.getInviteCode()));
    }
}
