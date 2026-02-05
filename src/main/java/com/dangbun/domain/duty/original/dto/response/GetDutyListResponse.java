package com.dangbun.domain.duty.original.dto.response;

import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.duty.refactor.domain.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyListResponse(
        @Schema(description = "당번 ID", example = "1")
        Long dutyId,
        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        String name,
        @Schema(description = "아이콘 코드", example = "BUCKET_PINK")
        DutyIcon icon
) {
    public static GetDutyListResponse of(Duty duty) {
        return new GetDutyListResponse(
                duty.getDutyId().value(),
                duty.getName(),
                duty.getIcon()
        );
    }
}
