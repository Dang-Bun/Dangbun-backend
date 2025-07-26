package com.dangbun.domain.cleaning.dto.response;

import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetCleaningDetailListResponse(
        @Schema(description = "청소 이름", example = "바닥 쓸기")
        String cleaningName,
        @Schema(description = "보여지는 멤버 이름 리스트", example = "[\"박완\", \"박한나\"]")
        List<String> displayedMemberNames,
        @Schema(description = "멤버 수", example = "3")
        int memberCount
) {
    public static GetCleaningDetailListResponse of(String cleaningName, List<String> displayedMemberNames, int memberCount) {
        return new GetCleaningDetailListResponse(cleaningName, displayedMemberNames, memberCount);
    }
}
