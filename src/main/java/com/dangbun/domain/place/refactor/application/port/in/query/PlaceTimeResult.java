package com.dangbun.domain.place.refactor.application.port.in.query;

import java.time.LocalTime;

public record PlaceTimeResult(
        LocalTime startTime,
        LocalTime endTime,
        Boolean isToday
) {
}
