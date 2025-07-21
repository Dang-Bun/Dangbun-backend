package com.dangbun.domain.duty.dto.response;

import com.dangbun.domain.duty.entity.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;

public record PutDutyUpdateResponse(
    @Schema(description = "생성된 당번 ID", example = "1")
    Long dutyId,
    @Schema(description = "당번 이름", example = "홀 청소 당번")
    String name,
    @Schema(description = "아이콘 코드", example = "CLEANER_PINK")
    DutyIcon icon

) {
        public static PutDutyUpdateResponse of(Long dutyId, String name, DutyIcon icon) {
            return new PutDutyUpdateResponse(dutyId, name, icon);
        }
}