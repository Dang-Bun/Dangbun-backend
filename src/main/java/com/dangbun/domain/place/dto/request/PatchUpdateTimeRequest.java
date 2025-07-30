package com.dangbun.domain.place.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;


public record PatchUpdateTimeRequest(
        @Schema(description = "시작 시간", example = "00:00")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull
        LocalTime starTime,

        @Schema(description = "종료 시간", example = "23:59")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull
        LocalTime endTime,

        @Schema(description = "endTime이 당일인가", example = "true")
        @NotNull
        Boolean isToday
) {
}
