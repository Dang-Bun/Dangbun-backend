package com.dangbun.domain.calendar.adapter.in.web;

import com.dangbun.common.hexagonal.WebAdapter;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.PatchUpdateChecklistToCompleteResponse;
import com.dangbun.domain.calendar.application.port.in.command.CalendarCommandUseCase;
import com.dangbun.domain.calendar.response.status.CalendarExceptionResponse;
import com.dangbun.global.aop.CheckManagerAuthority;
import com.dangbun.global.aop.CheckPlaceMembership;
import com.dangbun.global.docs.DocumentedApiErrors;
import com.dangbun.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Validated
@Tag(name = "Calendar", description = "CalendarController - 캘린더 관련 API")
@CheckPlaceMembership()
@RequiredArgsConstructor
@WebAdapter(path = "/places/{placeId}/calendar")
public class CalendarCommandController {

    private final CalendarCommandUseCase calendarCommandUseCase;

    @Operation(summary = "체크리스트 완료(매니저)")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE"}
    )
    @PatchMapping("/{checklistId}/complete")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<PatchUpdateChecklistToCompleteResponse>> updateChecklistToComplete(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        return ResponseEntity.ok(BaseResponse.ok(calendarCommandUseCase.finishChecklist(checklistId)));
    }

    @Operation(summary = "청소 삭제 (매니저용)")
    @DocumentedApiErrors(
            value = {CalendarExceptionResponse.class},
            includes = {"INVALID_ROLE"}
    )
    @DeleteMapping("/{checklistId}")
    @CheckManagerAuthority
    public ResponseEntity<BaseResponse<Void>> deleteChecklist(
            @PathVariable Long placeId,
            @PathVariable Long checklistId
    ) {
        calendarCommandUseCase.deleteChecklist(checklistId);
        return ResponseEntity.ok(BaseResponse.ok(null));
    }
}
