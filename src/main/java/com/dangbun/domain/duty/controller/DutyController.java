package com.dangbun.domain.duty.controller;

import com.dangbun.domain.duty.dto.request.PostDutyCreateRequest;
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

}
