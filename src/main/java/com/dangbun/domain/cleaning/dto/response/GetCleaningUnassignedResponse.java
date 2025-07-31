package com.dangbun.domain.cleaning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetCleaningUnassignedResponse(
        @Schema(description = "미지정 청소 이름", example = "바닥 닦기")
        String cleaningName
) {
    public static GetCleaningUnassignedResponse of(String cleaningName) {
        return new GetCleaningUnassignedResponse(cleaningName);
    }
}