package com.dangbun.domain.calender.controller;

import com.dangbun.domain.calender.dto.GetChecklistsResponse;
import com.dangbun.domain.calender.response.status.CalenderExceptionResponse;
import com.dangbun.domain.calender.service.CalenderService;
import com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse;
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
                                                                                   @RequestParam LocalDate date){
        return ResponseEntity.ok(BaseResponse.ok(calenderService.getChecklists(placeId,date)));
    }
}
