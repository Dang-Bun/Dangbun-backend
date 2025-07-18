package com.dangbun.domain.duty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostDutyCreateResponse (
        @Schema(description = "생성된 당번 ID", example = "1")
        Long dutyId
) {
        public static PostDutyCreateResponse of(Long dutyId) {
                return new PostDutyCreateResponse(dutyId);
        }
}
