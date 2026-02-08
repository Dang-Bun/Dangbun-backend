package com.dangbun.domain.place.application.port.in.query;

import java.util.List;

public record DutyProgressResult(
        List<DutyProgressDto> dutyProgressDtos
) {
    public record DutyProgressDto(
            Long dutyId,
            String dutyName,
            Long totalCleaning,
            Long endCleaning
    ) {
    }
}
