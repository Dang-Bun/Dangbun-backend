package com.dangbun.domain.cleaning.dto.response;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;

public record GetCleaningListResponse(
        Long dutyId,
        String name,
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
