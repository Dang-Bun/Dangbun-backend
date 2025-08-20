package com.dangbun.domain.cleaning.dto.request;

import com.dangbun.domain.cleaning.entity.CleaningRepeatType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.util.List;

public record PutCleaningUpdateRequest(
        @Schema(description = "청소 이름", example = "바닥 쓸기")
        @NotEmpty
        String cleaningName,

        @Schema(description = "당번 Id", example = "1")
        Long dutyId,

        @Schema(description = "담당 멤버", example = "[\"박완\", \"박한나\"]")
        List<String> members,

        @Schema(description = "사진 인증", example = "true")
        @NotNull
        Boolean needPhoto,

        @Schema(description = "청소 주기", example = "DAILY")
        @NotNull
        CleaningRepeatType repeatType,

        @Schema(description = "청소 반복 요일 목록 (repeatType = WEEKLY일 때 사용)", example = "[\"MONDAY\", \"FRIDAY\"]")
        List<DayOfWeek> repeatDays,

        @Schema(description = "선택한 청소 날짜 목록", example = "[\"2025-08-01\", \"2025-08-08\"]")
        @NotEmpty
        List<String> detailDates
) {
}
