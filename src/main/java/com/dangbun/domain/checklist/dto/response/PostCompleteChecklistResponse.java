package com.dangbun.domain.checklist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record PostCompleteChecklistResponse(
        @Schema(description = "청소를 종료한 맴버 이름", example = "홍길동")
        String memberName,

        @Schema(description = "종료시간", example = "11:30")
        LocalTime endTime
){
        public static PostCompleteChecklistResponse of(String name, LocalTime time){
                return new PostCompleteChecklistResponse(name, time);
        }
}
