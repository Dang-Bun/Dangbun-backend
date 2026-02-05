package com.dangbun.domain.duty.refactor.application.port.in.query;

import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.duty.refactor.domain.DutyIcon;

public record UpdateDutyResult(
        Long dutyId,
        String name,
        DutyIcon icon
) {
    public static UpdateDutyResult from(Duty duty) {
        return new UpdateDutyResult(
                duty.getDutyId().value(),
                duty.getName(),
                duty.getIcon()
        );
    }
}
