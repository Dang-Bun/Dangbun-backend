package com.dangbun.domain.place.application.port.in.query;

import java.time.LocalTime;

public record PlaceTimeResult(
        LocalTime startTime,
        LocalTime endTime,
        Boolean isToday
) {
}
