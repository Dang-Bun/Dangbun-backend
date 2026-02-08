package com.dangbun.domain.place.application.port.in.command;

import java.time.LocalTime;

public record UpdateTimeResult(
        LocalTime startTime,
        LocalTime endTime,
        Boolean isToday
) {
    public static UpdateTimeResult of(LocalTime startTime,
                                   LocalTime endTime,
                                   Boolean isToday) {
        return new UpdateTimeResult(startTime, endTime, isToday);
    }
}
