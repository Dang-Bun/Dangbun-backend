package com.dangbun.domain.duty.dto.response;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.entity.DutyIcon;

public record GetDutyListResponse(
        Long id,
        String name,
        DutyIcon icon
) {
    public static GetDutyListResponse of(Duty duty) {
        return new GetDutyListResponse(
                duty.getDutyId(),
                duty.getName(),
                duty.getIcon()
        );
    }
}
