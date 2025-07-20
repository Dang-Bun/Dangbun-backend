package com.dangbun.domain.duty.controller;

import com.dangbun.domain.duty.dto.request.PostDutyCreateRequest;
import com.dangbun.domain.duty.dto.response.GetDutyListResponse;
import com.dangbun.domain.duty.dto.response.PostDutyCreateResponse;
import com.dangbun.domain.duty.service.DutyService;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@Tag(name = "Duty", description = "DutyController - 당번 관련 API")
@RestController
@RequiredArgsConstructor
public class DutyController {

    private final DutyService dutyService;

    @Operation(summary = "당번 생성", description = "플레이스에 새로운 당번을 생성합니다.")
    @PostMapping("/places/{placeId}/duties")
    public ResponseEntity<BaseResponse<PostDutyCreateResponse>> createDuty(
            @PathVariable Long placeId,
            @RequestBody @Valid PostDutyCreateRequest request
    ) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.createDuty(placeId, request)));
    }

    @Operation(summary = "당번 목록 조회", description = "해당 플레이스의 당번 목록을 조회합니다.")
    @GetMapping("/places/{placeId}/duties")
    public ResponseEntity<BaseResponse<List<GetDutyListResponse>>> getDutyList(@PathVariable Long placeId) {
        return ResponseEntity.ok(BaseResponse.ok(dutyService.getDutyList(placeId)));
    }

}
