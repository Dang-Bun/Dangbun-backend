package com.dangbun.domain.calendar.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record GetProgressBarsResponse (
    List<DailyProgressDto> dailyProgress
){

    public static GetProgressBarsResponse of(List<DailyProgressDto> dailyProgress){
        return new GetProgressBarsResponse(dailyProgress);
    }

    public record DailyProgressDto(
            @Schema(description = "날짜")
            LocalDate date,

            @Schema(description = "해당 날의 청소 수", example = "10")
            int totalCleaning,

            @Schema(description = "해당 날의 완료된 청소 수", example = "7")
            int endCleaning,

            @Schema(description = "완료 퍼센트", example = "70")
            double endPercent
    ){
        public static DailyProgressDto of(LocalDate date, int totalCleaning, int endCleaning){
            double endPercent = ((double) endCleaning /totalCleaning)*100;
            return new DailyProgressDto(date, totalCleaning, endCleaning, endPercent);
        }
    }
}
