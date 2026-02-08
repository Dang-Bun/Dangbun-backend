package com.dangbun.domain.duty.adapter.in.web.dto.response;

import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostAddCleaningsResponse(
        @Schema(description = "추가된 청소 ID 리스트", example = "[1, 2, 3]")
        List<Long> assignedCleaningIds
) {
    public static PostAddCleaningsResponse from(AddCleaningsResult result) {
        return new PostAddCleaningsResponse(result.assignedCleaningIds());
    }
}
