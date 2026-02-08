package com.dangbun.domain.duty.adapter.in.web.dto.response;

import com.dangbun.domain.duty.application.port.in.query.DutyCleaningsResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyCleaningNameListResponse(
        @Schema(description = "청소 ID", example = "1")
        Long cleaningId,
        @Schema(description = "청소 이름", example = "바닥 쓸기")
        String name
) {
    public static GetDutyCleaningNameListResponse from(DutyCleaningsResult.CleaningItem item) {
        return new GetDutyCleaningNameListResponse(
                item.cleaningId(),
                item.name()
        );
    }
}
