package com.dangbun.domain.place.controller;

import com.dangbun.domain.place.dto.GetPlaceListResponse;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.service.PlaceService;
import com.dangbun.domain.user.entity.User;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/places")
@RequiredArgsConstructor
public class PlaceController {

    private final PlaceService placeService;

    @Operation(summary = " 모든 플레이스 조회", description = "사용자의 모든 플레이스를 조회하기 위해 사용됩니다(플레이스 선택 화면)")
    @GetMapping()
    public ResponseEntity<?> getPlaces(@AuthenticationPrincipal(expression = "user") User user){
        List<GetPlaceListResponse> places = placeService.getPlaces(user.getId());
        return ResponseEntity.ok(BaseResponse.ok(places));
    }
}
