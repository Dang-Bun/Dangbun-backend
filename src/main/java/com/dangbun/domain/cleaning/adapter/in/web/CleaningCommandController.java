package com.dangbun.domain.cleaning.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;

import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PostCleaningCreateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.request.PutCleaningUpdateRequest;
import com.dangbun.domain.cleaning.adapter.in.web.dto.response.PostCleaningResponse;
import com.dangbun.domain.cleaning.application.port.in.command.CleaningCommandUseCase;
import com.dangbun.domain.cleaning.exception.status.CleaningExceptionResponse;
import com.dangbun.domain.member.exception.status.MemberExceptionResponse;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@Tag(name = "Cleaning", description = "CleaningController - 청소 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}")
public class CleaningCommandController {

    private final CleaningCommandUseCase cleaningCommandUseCase;

    @Operation(summary = "당번별 청소 생성", description = "입력한 정보들을 바탕으로 새로운 청소를 생성합니다. (매니저용)")
    @PostMapping("/cleanings")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "DUTY_NOT_FOUND", "CLEANING_ALREADY_EXISTS", "INVALID_DATE_FORMAT"}
    )
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PostCleaningResponse>> createCleaning(
            @PathVariable Long placeId,
            @RequestBody @Valid PostCleaningCreateRequest request) {

        return ResponseEntity.ok(BaseResponse.ok(cleaningCommandUseCase.createCleaning(request)));
    }

    @Operation(summary = "당번별 청소 수정", description = "입력한 정보들을 바탕으로 청소를 수정합니다. (매니저용)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "CLEANING_NOT_FOUND", "DUTY_NOT_FOUND", "INVALID_DATE_FORMAT", "CLEANING_ALREADY_EXISTS"}
    )
    @PutMapping("/cleanings/{cleaningId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> updateCleaning(
            @PathVariable Long placeId,
            @PathVariable Long cleaningId,
            @Valid @RequestBody PutCleaningUpdateRequest request
    ) {
        cleaningCommandUseCase.updateCleaning(cleaningId, request);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }

    @Operation(summary = "청소 삭제", description = "청소 항목을 삭제합니다. (매니저용)")
    @DocumentedApiErrors(
            value = {MemberExceptionResponse.class, CleaningExceptionResponse.class},
            includes = {"MEMBERSHIP_UNAUTHORIZED", "PLACE_ACCESS_DENIED", "INVALID_ROLE", "CLEANING_NOT_FOUND"}
    )
    @DeleteMapping("/cleanings/{cleaningId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> deleteCleaning(
            @PathVariable Long placeId,
            @PathVariable Long cleaningId) {
        cleaningCommandUseCase.deleteCleaning(cleaningId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
