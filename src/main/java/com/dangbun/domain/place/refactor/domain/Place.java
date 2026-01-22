package com.dangbun.domain.place.refactor.domain;

import com.dangbun.domain.place.original.entity.PlaceCategory;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

import java.time.LocalTime;

@AllArgsConstructor
public class Place {

    @Getter
    private final PlaceId placeId;

    @Getter
    private String name;

    @Getter
    private PlaceCategory category;

    @Getter
    private String categoryName;

    @Getter
    private String inviteCode;

    @Getter
    private LocalTime startTime = LocalTime.MIDNIGHT;

    @Getter
    private LocalTime endTime = LocalTime.of(23, 59);

    @Getter
    private Boolean isToday = true;


    @Builder
    public static Place withoutId(String name, PlaceCategory category, String categoryName) {
        return new Place(null, name, category, categoryName, null, null, null, null);
    }

    @Builder
    public static Place withId(PlaceId placeId, String name, PlaceCategory category, String categoryName) {
        return new Place(placeId, name, category, categoryName, null, null, null, null);
    }

    public String createCode(String code) {
        if (this.inviteCode != null)
            return this.inviteCode;

        this.inviteCode = code;
        return this.inviteCode;
    }

    public void setTime(LocalTime startTime, LocalTime endTime, Boolean isToday) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isToday = isToday;
    }

    @Value
    public static class PlaceId {
        private Long value;
    }
}
