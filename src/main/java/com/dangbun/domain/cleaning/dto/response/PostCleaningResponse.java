package com.dangbun.domain.cleaning.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCleaningResponse(
        @Schema(description = "생성된 청소 ID", example = "1")
        Long cleaningId
) {
    public static PostCleaningResponse of(Long cleaningId) {
        return new PostCleaningResponse(cleaningId);
    }
}