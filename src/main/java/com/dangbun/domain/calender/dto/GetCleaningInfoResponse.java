package com.dangbun.domain.calender.dto;

import com.dangbun.domain.cleaning.entity.CleaningRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public record GetCleaningInfoResponse(
        @Schema(description = "청소 Id", example = "1")
        Long cleaningId,

        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        String dutyName,

        @Schema(description = "담당 맴버", example = "[김효정, 박완, 박한나]")
        List<String> membersName,

        @Schema(description = "사진 인증", example = "true")
        Boolean needPhoto,

        @Schema(description = "청소 반복 정보(DAILY, WEEKLY, MONTHLY_FIRST, MONTHLY_LAST, NONE)", example = "WEEKLY")
        CleaningRepeatType repeatType,

        @Schema(description = "청소 반복 정보: WEEKLY 일 경우", example = "[TUESDAY, FRIDAY]")
        List<String> repeatDays,

        @Schema(description = "세부 날짜", example = "[2025-08-07, 2025-08-12]")
        List<LocalDate> dates

) {
    public static GetCleaningInfoResponse of(Long cleaningId,
                                             String dutyName,
                                             List<String> membersName,
                                             Boolean needPhoto,
                                             CleaningRepeatType repeatType,
                                             List<DayOfWeek> repeatDays,
                                             List<LocalDate> dates) {
        List<String> repeatDayNames = repeatDays != null
                ? repeatDays.stream().map(DayOfWeek::name).toList()
                : List.of();

        return new GetCleaningInfoResponse(
                cleaningId,
                dutyName,
                membersName,
                needPhoto,
                repeatType,
                repeatDayNames,
                dates
        );
    }
}
