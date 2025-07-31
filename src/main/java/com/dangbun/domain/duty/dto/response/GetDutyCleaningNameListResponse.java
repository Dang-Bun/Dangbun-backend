package com.dangbun.domain.duty.dto.response;

import com.dangbun.domain.cleaning.entity.Cleaning;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyCleaningNameListResponse(
        @Schema(description = "청소 ID", example = "1")
        Long cleaningId,
        @Schema(description = "청소 이름", example = "바닥 쓸기")
        String name
) {
        public static GetDutyCleaningNameListResponse of(Cleaning cleaning) {
                return new GetDutyCleaningNameListResponse(cleaning.getCleaningId(), cleaning.getName());
        }
}