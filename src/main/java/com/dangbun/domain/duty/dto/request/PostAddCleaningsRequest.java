package com.dangbun.domain.duty.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostAddCleaningsRequest(
        @Schema(description = "추가할 청소 ID 리스트", example = "[1, 2, 3]")
        List<Long> cleaningIds
) {
}
