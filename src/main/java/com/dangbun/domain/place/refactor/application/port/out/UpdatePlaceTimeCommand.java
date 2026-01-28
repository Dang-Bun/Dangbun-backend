package com.dangbun.domain.place.refactor.application.port.out;

import java.time.LocalTime;

public record UpdatePlaceTimeCommand(
        Long placeId,
        LocalTime startTime,
        LocalTime endTime,
        boolean isToday
) {
    public static UpdatePlaceTimeCommand of(Long placeId, LocalTime startTime, LocalTime endTime, boolean isToday){
        return new UpdatePlaceTimeCommand(placeId, startTime, endTime, isToday);
    }
}
