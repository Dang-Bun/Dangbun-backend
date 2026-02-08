package com.dangbun.domain.calendar.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetChecklistsResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetCleaningInfoResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetProgressBarsResponse;
import com.dangbun.domain.calendar.application.port.in.query.CalendarQuery;
import com.dangbun.domain.calendar.response.status.CalendarExceptionResponse;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Validated
@Tag(name = "Calendar", description = "CalendarController - 캘린더 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/calendar")
public class CalendarQueryController {

    private final CalendarQuery calendarQuery;

    @Operation(summary = "날짜 조회")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"FUTURE_DATE_NOT_ALLOWED"}
    )
    @GetMapping("/checklists")
    public ResponseEntity<BaseResponse<GetChecklistsResponse>> getChecklistsByDate(
            @PathVariable Long placeId,
            @RequestParam LocalDate date
    ) {
        return ResponseEntity.ok(BaseResponse.ok(calendarQuery.getChecklists(date)));
    }

    @Operation(summary = "프로그래스바 조회(이전 달, 다음 달 포함)")
    @GetMapping()
    public ResponseEntity<BaseResponse<GetProgressBarsResponse>> getProgressBars(
            @PathVariable Long placeId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return ResponseEntity.ok(BaseResponse.ok(calendarQuery.getProgressBars(year, month)));
    }

    @Operation(summary = "사진 확인")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE", "NO_PHOTO"}
    )
    @GetMapping("/{checklistId}/photos")
    public ResponseEntity<BaseResponse<GetImageUrlResponse>> getImageUrl(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(calendarQuery.getPhotoUrl(checklistId)));
    }

    @Operation(summary = "청소 정보 확인")
    @GetMapping("/{checklistId}/cleanings")
    public ResponseEntity<BaseResponse<GetCleaningInfoResponse>> getCleaningInfo(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(calendarQuery.getCleaningInfo(checklistId)));
    }
}
