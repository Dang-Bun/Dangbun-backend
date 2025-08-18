package com.dangbun.domain.calendar.controller;

import com.dangbun.domain.calendar.dto.*;
import com.dangbun.domain.calendar.response.status.CalendarExceptionResponse;
import com.dangbun.domain.calendar.service.CalendarService;

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

import java.time.LocalDate;

@Validated
@CheckPlaceMembership()
@RequestMapping("/places/{placeId}/calendar")
@RequiredArgsConstructor
@Tag(name = "Calendar", description = "CalendarController - 캘린더 관련 API")
@RestController
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "날짜 조회")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"FUTURE_DATE_NOT_ALLOWED"}
    )
    @GetMapping("/checklists")
    public ResponseEntity<BaseResponse<GetChecklistsResponse>> getChecklistsByDate(@PathVariable Long placeId,
                                                                                   @RequestParam LocalDate date) {
        return ResponseEntity.ok(BaseResponse.ok(calendarService.getChecklists( date)));
    }

    @Operation(summary = "프로그래스바 조회(이전 달, 다음 달 포함)")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping()
    public ResponseEntity<BaseResponse<GetProgressBarsResponse>> getProgressBars(@PathVariable Long placeId,
                                                                                 @RequestParam int year,
                                                                                 @RequestParam int month) {
        return ResponseEntity.ok(BaseResponse.ok(calendarService.getProgressBars( year, month)));
    }

    @Operation(summary = "체크리스트 완료(매니저)")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE"}
    )
    @PatchMapping("/{checklistId}/complete")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PatchUpdateChecklistToCompleteResponse>> updateChecklistToComplete(@PathVariable Long placeId,
                                                                                                          @PathVariable Long checklistId){
        calendarService.finishChecklist(checklistId);
        return ResponseEntity.ok(null);
    }


    @Operation(summary = "사진 확인")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE","NO_PHOTO"}
    )
    @GetMapping("/{checklistId}/photos")
    public ResponseEntity<BaseResponse<GetImageUrlResponse>> getImageUrl(@PathVariable Long placeId,
                                                                         @PathVariable Long checklistId){
        return ResponseEntity.ok(BaseResponse.ok(calendarService.getPhotoUrl(checklistId)));
    }

    @Operation(summary = "청소 정보 확인")
    @DocumentedApiErrors(
            value = {},
            includes = {""}
    )
    @GetMapping("/{checklistId}/cleanings")
    public ResponseEntity<BaseResponse<GetCleaningInfoResponse>> getCleaningInfo(@PathVariable Long placeId,
                                                                                @PathVariable Long checklistId){
        return ResponseEntity.ok(BaseResponse.ok(calendarService.getCleaningInfo(checklistId)));
    }


    @Operation(summary = "청소 삭제 (매니저용)")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE"}
    )
    @DeleteMapping("/{checklistId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<?>> deleteChecklist(@PathVariable Long placeId,
                                                          @PathVariable Long checklistId){
        calendarService.deleteChecklist(checklistId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
