package com.dangbun.domain.duty.refactor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Duty {

    @Getter
    private final DutyId dutyId;

    @Getter
    private String name;

    @Getter
    private DutyIcon icon;

    @Getter
    private final Long placeId;

    public static Duty withoutId(String name, DutyIcon icon, Long placeId) {
        return new Duty(null, name, icon, placeId);
    }

    public static Duty withId(DutyId dutyId, String name, DutyIcon icon, Long placeId) {
        return new Duty(dutyId, name, icon, placeId);
    }

    public void update(String name, DutyIcon icon) {
        this.name = name;
        this.icon = icon;
    }

    public record DutyId(Long value) {
    }
}
