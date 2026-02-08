package com.dangbun.domain.duty.adapter.in.web.dto.response;

import com.dangbun.domain.duty.application.port.in.query.DutyListResult;
import com.dangbun.domain.duty.domain.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyListResponse(
        @Schema(description = "당번 ID", example = "1")
        Long dutyId,
        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        String name,
        @Schema(description = "아이콘 코드", example = "BUCKET_PINK")
        DutyIcon icon
) {
    public static GetDutyListResponse from(DutyListResult.DutyItem item) {
        return new GetDutyListResponse(
                item.dutyId(),
                item.name(),
                item.icon()
        );
    }
}
