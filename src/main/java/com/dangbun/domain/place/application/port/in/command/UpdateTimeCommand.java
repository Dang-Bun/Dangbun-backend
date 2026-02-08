package com.dangbun.domain.place.application.port.in.command;

import java.time.LocalTime;

public record UpdateTimeCommand(
        LocalTime startTime,
        LocalTime endTime,
        Boolean isToday
) {
    public static UpdateTimeCommand of (LocalTime startTime, LocalTime endTime, Boolean isToday){
        return new UpdateTimeCommand(startTime, endTime, isToday);
    }
}
