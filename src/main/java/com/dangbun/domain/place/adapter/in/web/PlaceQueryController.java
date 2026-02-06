package com.dangbun.domain.place.adapter.in.web;

import com.dangbun.domain.member.original.response.status.MemberExceptionResponse;
import com.dangbun.domain.place.adapter.in.web.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.adapter.in.web.dto.response.*;
import com.dangbun.domain.place.application.port.in.query.*;
import com.dangbun.domain.place.refactor.adapter.in.web.dto.response.*;
import com.dangbun.domain.place.exception.status.PlaceExceptionResponse;
import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.place.refactor.application.port.in.query.*;
import com.dangbun.domain.place.domain.Information;
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

import java.util.List;


@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Place", description = "PlaceController - 플레이스 관련 API")
@WebAdapter(path = "/places")
public class PlaceQueryController {

    private final PlaceQuery placeQuery;

    @Operation(summary = " 플레이스 목록 조회", description = "사용자의 모든 플레이스를 조회하기 위해 사용됩니다(플레이스 선택 화면)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping()
    public ResponseEntity<BaseResponse<GetPlaceListResponse>> getPlaces(@AuthenticationPrincipal CustomUserDetails userDetails) {
        PlaceListResult result = placeQuery.getPlaceList(userDetails.getUser().getUserId());

        List<GetPlaceListResponse.PlaceDto> placeDtos = result.places().stream()
                .map(p -> GetPlaceListResponse.PlaceDto.of(
                        p.placeId(),
                        p.name(),
                        p.category(),
                        p.categoryName(),
                        p.totalCleaning(),
                        p.endCleaning(),
                        p.role(),
                        p.notifyNumber()
                ))
                .toList();

        return ResponseEntity.ok(BaseResponse.ok(GetPlaceListResponse.of(placeDtos)));
    }


    @Operation(summary = "참여코드 확인", description = "참여코드를 입력합니다. 성공적으로 입력할 시 정보 입력 창이 뜹니다.")
    @DocumentedApiErrors(
            value = {PlaceExceptionResponse.class},
            includes = {"ALREADY_INVITED", "INVALID_INVITE_CODE"}
    )
    @PostMapping("/invite-code")
    public ResponseEntity<BaseResponse<PostCheckInviteCodeResponse>> checkInviteCode(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                                                     @RequestBody PostCheckInviteCodeRequest request) {
        Information information = placeQuery.checkInviteCode(userDetails.getUser().getUserId(), request.inviteCode());

        PostCheckInviteCodeResponse response = PostCheckInviteCodeResponse.of(
                information.getPlaceId(),
                information.getInformation()
        );
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
        PlaceResult result = placeQuery.getPlace();

        List<GetPlaceResponse.DutyDto> dutyDtos = null;
        if (result.duty() != null) {
            PlaceResult.DutyDto duty = result.duty();

            List<GetPlaceResponse.CheckListDto> checkListDtos = duty.checkLists().stream()
                    .map(cl -> new GetPlaceResponse.CheckListDto(
                            cl.checkListId(),
                            cl.members().stream()
                                    .map(m -> new GetPlaceResponse.MemberDto(m.memberId(), m.memberName()))
                                    .toList(),
                            cl.cleaningName(),
                            cl.completeTime(),
                            cl.needPhoto()
                    ))
                    .toList();

            dutyDtos = List.of(new GetPlaceResponse.DutyDto(
                    duty.dutyId(),
                    duty.dutyName(),
                    duty.totalCleaning(),
                    duty.endCleaning(),
                    checkListDtos
            ));
        }

        GetPlaceResponse response = new GetPlaceResponse(
                result.memberId(),
                result.placeId(),
                result.placeName(),
                result.placeCategory(),
                result.categoryName(),
                result.endTime(),
                dutyDtos
        );

        return ResponseEntity.ok(BaseResponse.ok(response));
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
        PlaceTimeResult result = placeQuery.getTimeAndIsToday();

        GetTimeResponse response = GetTimeResponse.of(
                result.startTime(),
                result.endTime(),
                result.isToday()
        );
        return ResponseEntity.ok(BaseResponse.ok(response));
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
        DutyProgressResult result = placeQuery.getDutiesProgress();

        List<DutyProgressDto> dutyProgressDtos = result.dutyProgressDtos().stream()
                .map(dto -> DutyProgressDto.of(
                        dto.dutyId(),
                        dto.dutyName(),
                        dto.totalCleaning(),
                        dto.endCleaning()
                ))
                .toList();

        return ResponseEntity.ok(BaseResponse.ok(GetDutiesProgressResponse.of(dutyProgressDtos)));
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
        String inviteCode = placeQuery.getInviteCode();

        return ResponseEntity.ok(BaseResponse.ok(GetPlaceInvitedCodeResponse.of(inviteCode)));
    }
}
