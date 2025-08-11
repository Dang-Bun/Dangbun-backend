package com.dangbun.domain.place.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record GetTimeResponse (
        @Schema(description = "시작 시간", example = "00:00", type = "string", pattern = "HH:mm")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @Schema(description = "종료 시간", example = "23:59", type = "string", pattern = "HH:mm")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime,

        @Schema(description = "endTime이 당일인가", example = "true")
        Boolean isToday
){
    public static GetTimeResponse of(LocalTime startTime, LocalTime endTime, boolean isToday) {
        return new GetTimeResponse(startTime,endTime, isToday);
    }
}
