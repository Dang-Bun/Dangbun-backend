package com.dangbun.domain.calendar.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

public record GetChecklistsResponse (
        @Schema(description = "체크리스트")
        List<ChecklistDto> checklists
){
    public static GetChecklistsResponse of(List<ChecklistDto> checklistDtos){
        return new GetChecklistsResponse(checklistDtos);
    }

    @Schema(name = "GetChecklistsResponse.ChecklistDto", description = "체크리스트 DTO")
    public record ChecklistDto(
            @Schema(description = "체크리스트 Id", example = "1")
            Long checklistId,

            @Schema(description = "당번 이름", example = "당번 A")
            String dutyName,

            @Schema(description = "청소 완료 여부", example = "true")
            Boolean isComplete,

            @Schema(description = "완료한 사람 이름", example = "홍길동")
            String memberName,

            @Schema(description = "완료한 시각", example = "01:56")
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
            LocalTime endTime,

            @Schema(description = "사진 요구", example = "true")
            Boolean needPhoto

    ){
        public static ChecklistDto of(Long checklistId, String dutyName, Boolean isComplete, String memberName, LocalTime endTime, Boolean needPhoto){
            return new ChecklistDto(checklistId, dutyName, isComplete, memberName, endTime, needPhoto);
        }
    }
}
