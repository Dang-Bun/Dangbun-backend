package com.dangbun.domain.cleaning.dto.response;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetCleaningListResponse(
        @Schema(description = "당번 ID", example = "1")
        Long dutyId,
        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        String name,
        @Schema(description = "당번 아이콘", example = "BUCKET_PINK")
        DutyIcon icon
) {
    public static GetCleaningListResponse of(Duty duty) {
        return new GetCleaningListResponse(
                duty.getDutyId(),
                duty.getName(),
                duty.getIcon()
        );
    }
}
