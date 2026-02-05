package com.dangbun.domain.cleaning.refactor.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class Cleaning {

    @Getter
    private final CleaningId cleaningId;

    @Getter
    private String name;

    @Getter
    private CleaningRepeatType repeatType;

    @Getter
    private String repeatDays;

    @Getter
    private Long dutyId;

    @Getter
    private Boolean needPhoto;

    @Getter
    private final Long placeId;

    public static Cleaning withoutId(
            String name,
            CleaningRepeatType repeatType,
            String repeatDays,
            Long dutyId,
            Boolean needPhoto,
            Long placeId
    ) {
        return new Cleaning(null, name, repeatType, repeatDays, dutyId, needPhoto, placeId);
    }

    public static Cleaning withId(
            CleaningId cleaningId,
            String name,
            CleaningRepeatType repeatType,
            String repeatDays,
            Long dutyId,
            Boolean needPhoto,
            Long placeId
    ) {
        return new Cleaning(cleaningId, name, repeatType, repeatDays, dutyId, needPhoto, placeId);
    }

    public void update(String name, Boolean needPhoto, CleaningRepeatType repeatType, String repeatDays, Long dutyId) {
        this.name = name;
        this.needPhoto = needPhoto;
        this.repeatType = repeatType;
        this.repeatDays = repeatDays;
        this.dutyId = dutyId;
    }

    public void assignToDuty(Long dutyId) {
        this.dutyId = dutyId;
    }

    public void removeDuty() {
        this.dutyId = null;
    }

    public record CleaningId(Long value) {
    }
}
