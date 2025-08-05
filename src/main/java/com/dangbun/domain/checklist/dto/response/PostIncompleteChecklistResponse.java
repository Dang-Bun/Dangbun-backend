package com.dangbun.domain.checklist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

public record PostIncompleteChecklistResponse(
        @Schema(description = "체크리스트 Id", example = "1")
        Long checkListId,

        @Schema(description = "청소를 종료한 맴버 이름", example = "[홍길동, 김철수]")
        List<String> membersName,

        @Schema(description = "종료시간", example = "11:30")
        LocalTime endTime
){
    public static PostIncompleteChecklistResponse of(Long checkListId, List<String> membersName, LocalTime endTime){
        return new PostIncompleteChecklistResponse(checkListId, membersName, endTime);
    }
}
