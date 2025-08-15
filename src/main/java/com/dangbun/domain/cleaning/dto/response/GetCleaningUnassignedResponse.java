package com.dangbun.domain.cleaning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetCleaningUnassignedResponse(
        @Schema(description = "청소 Id", example = "1")
        Long cleaningId,

        @Schema(description = "미지정 청소 이름", example = "바닥 닦기")
        String cleaningName
) {
    public static GetCleaningUnassignedResponse of(Long cleaningId, String cleaningName) {
        return new GetCleaningUnassignedResponse(cleaningId, cleaningName);
    }
}