package com.dangbun.domain.duty.refactor.adapter.in.web.dto.response;

import com.dangbun.domain.duty.refactor.application.port.in.query.UpdateDutyResult;
import com.dangbun.domain.duty.refactor.domain.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;

public record PutDutyUpdateResponse(
        @Schema(description = "당번 ID", example = "1")
        Long dutyId,
        @Schema(description = "당번 이름", example = "홀 청소 당번")
        String name,
        @Schema(description = "아이콘 코드", example = "CLEANER_PINK")
        DutyIcon icon
) {
    public static PutDutyUpdateResponse from(UpdateDutyResult result) {
        return new PutDutyUpdateResponse(
                result.dutyId(),
                result.name(),
                result.icon()
        );
    }
}
