package com.dangbun.domain.calender.controller;

import com.dangbun.domain.calender.dto.GetChecklistsResponse;
import com.dangbun.domain.calender.dto.GetProgressBarsResponse;
import com.dangbun.domain.calender.dto.PatchUpdateChecklistToCompleteResponse;
import com.dangbun.domain.calender.response.status.CalenderExceptionResponse;
import com.dangbun.domain.calender.service.CalenderService;
import com.dangbun.global.CheckPlaceMembership;
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
@CheckPlaceMembership(placeIdParam = "placeId")
@RequestMapping("/place/{placeId}/calender")
@RequiredArgsConstructor
@Tag(name = "Calender", description = "CalenderController - 캘린더 관련 API")
@RestController
public class CalenderController {

    private final CalenderService calenderService;

    @Operation(summary = "날짜 조회")
    @DocumentedApiErrors(
            value = {CalenderExceptionResponse.class},
            includes = {"FUTURE_DATE_NOT_ALLOWED"}
    )
    @GetMapping("/checklists")
    public ResponseEntity<BaseResponse<GetChecklistsResponse>> getChecklistsByDate(@PathVariable Long placeId,
                                                                                   @RequestParam LocalDate date) {
        return ResponseEntity.ok(BaseResponse.ok(calenderService.getChecklists(placeId, date)));
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
        return ResponseEntity.ok(BaseResponse.ok(calenderService.getProgressBars(placeId, year, month)));
    }

    @Operation(summary = "체크리스트 완료(매니저)")
    @DocumentedApiErrors(
            value = {CalenderExceptionResponse.class},
            includes = {"INVALID_ROLE"}
    )
    @PatchMapping("/{checklistId}/complete")
    public ResponseEntity<BaseResponse<PatchUpdateChecklistToCompleteResponse>> updateChecklistToComplete(@PathVariable Long placeId,
                                                                                                          @PathVariable Long checklistId){
        calenderService.finishChecklist(placeId, checklistId);
        return ResponseEntity.ok(null);
    }


    //사진 확인

    // 청소 정보

    // 청소 삭제
}
