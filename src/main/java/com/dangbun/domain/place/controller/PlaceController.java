package com.dangbun.domain.place.controller;

import com.dangbun.domain.place.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.dto.request.PostCreatePlaceRequest;
import com.dangbun.domain.place.dto.request.PostRegisterPlaceRequest;
import com.dangbun.domain.place.dto.response.GetPlaceListResponse;
import com.dangbun.domain.place.dto.response.PostCheckInviteCodeResponse;
import com.dangbun.domain.place.dto.response.PostCreateInviteCodeResponse;
import com.dangbun.domain.place.service.PlaceService;
import com.dangbun.domain.user.entity.User;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = " 플레이스 목록 조회", description = "사용자의 모든 플레이스를 조회하기 위해 사용됩니다(플레이스 선택 화면)")
    @GetMapping()
    public ResponseEntity<?> getPlaces(@AuthenticationPrincipal(expression = "user") User user){
        List<GetPlaceListResponse> places = placeService.getPlaces(user.getId());
        return ResponseEntity.ok(BaseResponse.ok(places));
    }

    @Operation(summary = "플레이스 생성", description = "플레이스를 생성합니다. 플레이스를 생성한 user는 매니저가 됩니다.")
    @PostMapping
    public ResponseEntity<?> createPlace(@AuthenticationPrincipal(expression = "user") User user,
                                         @Valid @RequestBody PostCreatePlaceRequest request){

        placeService.createPlaceWithManager(user.getId(), request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "참여코드 생성", description = "플레이스의 참여코드를 생성합니다.")
    @PostMapping("/{placeId}/invite-code")
    public ResponseEntity<?> createInviteCode(@AuthenticationPrincipal(expression = "user") User user,
                                              @PathVariable Long placeId){
        PostCreateInviteCodeResponse data = placeService.createInviteCode(user.getId(), placeId);
        return ResponseEntity.ok(BaseResponse.ok(data));
    }

    @Operation(summary = "참여코드 확인", description = "참여코드를 입력합니다. 성공적으로 입력할 시 정보 입력 창이 뜹니다.")
    @PostMapping("/invite-code")
    public ResponseEntity<?> checkInviteCode(@AuthenticationPrincipal(expression = "user") User user,
                                             @RequestBody PostCheckInviteCodeRequest request){

       PostCheckInviteCodeResponse response = placeService.checkInviteCode(user,request);
       return ResponseEntity.ok(BaseResponse.ok(response));

    }

    @Operation(summary = "참여 신청", description = "플레이스에 참가 신청합니다. 플레이스가 요구한 정보들을 입력해야합니다.")
    @PostMapping("/join-requests")
    public ResponseEntity<?> registerPlace(@AuthenticationPrincipal(expression = "user") User user,
                                           @RequestBody PostRegisterPlaceRequest request){

        placeService.joinRequest(user, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
