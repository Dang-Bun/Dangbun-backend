package com.dangbun.domain.duty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetCleanigListResponse (
        @Schema(description = "청소 ID", example = "1")
        Long cleaningId,
        @Schema(description = "청소 이름", example = "바닥 닦기")
        String cleaningName,
        @Schema(description = "보여지는 멤버 이름 리스트", example = "[\"박완\", \"박한나\"]")
        List<String> displayedNames,
        @Schema(description = "청소 멤버 수", example = "3")
        int memberCount,
        @Schema(description = "공통 여부", example = "true")
        boolean isCommon
) {
    public static GetCleanigListResponse of(Long cleaningId, String name, List<String> displayednames, int size, boolean isCommon) {
        return new GetCleanigListResponse(
                cleaningId, name, displayednames, size, isCommon
        );
    }
}
