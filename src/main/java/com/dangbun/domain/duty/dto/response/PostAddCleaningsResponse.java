package com.dangbun.domain.duty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostAddCleaningsResponse(
        @Schema(description = "추가된 청소 ID 목록")
        List<Long> addedCleaningId
) {
    public static PostAddCleaningsResponse of(List<Long> addedMemberId) {
        return new PostAddCleaningsResponse(addedMemberId);
    }
}