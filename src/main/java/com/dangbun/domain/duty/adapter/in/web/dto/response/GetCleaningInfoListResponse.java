package com.dangbun.domain.duty.adapter.in.web.dto.response;

import com.dangbun.domain.duty.application.port.in.query.CleaningInfoListResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetCleaningInfoListResponse(
        @Schema(description = "청소 ID", example = "1")
        Long cleaningId,
        @Schema(description = "청소 이름", example = "바닥 닦기")
        String cleaningName,
        @Schema(description = "보여지는 멤버 이름 리스트", example = "[\"박완\", \"박한나\"]")
        List<String> displayedNames,
        @Schema(description = "청소 멤버 수", example = "3")
        int memberCount
) {
    public static GetCleaningInfoListResponse from(CleaningInfoListResult.CleaningInfo info) {
        return new GetCleaningInfoListResponse(
                info.cleaningId(),
                info.cleaningName(),
                info.displayedNames(),
                info.memberCount()
        );
    }
}
