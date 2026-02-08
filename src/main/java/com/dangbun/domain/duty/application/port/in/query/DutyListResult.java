package com.dangbun.domain.duty.application.port.in.query;

import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.duty.domain.DutyIcon;

import java.util.List;

public record DutyListResult(List<DutyItem> duties) {

    public record DutyItem(
            Long dutyId,
            String name,
            DutyIcon icon
    ) {
        public static DutyItem from(Duty duty) {
            return new DutyItem(
                    duty.getDutyId().value(),
                    duty.getName(),
                    duty.getIcon()
            );
        }
    }

    public static DutyListResult from(List<Duty> duties) {
        return new DutyListResult(
                duties.stream()
                        .map(DutyItem::from)
                        .toList()
        );
    }
}
