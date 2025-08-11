package com.dangbun.domain.place.dto.response;

import com.dangbun.domain.place.entity.Place;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record PatchUpdateTimeResponse(
        @Schema(description = "시작 시간", example = "00:00", type = "string", pattern = "HH:mm")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime startTime,

        @Schema(description = "종료 시간", example = "23:59", type = "string", pattern = "HH:mm")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        LocalTime endTime,

        @Schema(description = "endTime이 당일인가", example = "true")
        Boolean isToday
) {
    public static PatchUpdateTimeResponse of(Place place, Boolean isToday){
        return new PatchUpdateTimeResponse(place.getStartTime(), place.getEndTime(), isToday);
    }
}
