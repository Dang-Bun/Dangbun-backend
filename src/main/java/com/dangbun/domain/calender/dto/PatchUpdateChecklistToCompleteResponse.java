package com.dangbun.domain.calender.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record PatchUpdateChecklistToCompleteResponse(
        @Schema(description = "완료한 맴버 이름", example = "홍길동")
        String memberName,

        @Schema(description = "완료한 시간")
        LocalTime endTime
) {
    public static PatchUpdateChecklistToCompleteResponse of(String memberName, LocalTime endTime) {
        return new PatchUpdateChecklistToCompleteResponse(memberName, endTime);
    }
}
