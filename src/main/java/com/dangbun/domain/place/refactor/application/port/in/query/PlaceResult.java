package com.dangbun.domain.place.refactor.application.port.in.query;

import com.dangbun.domain.place.original.entity.PlaceCategory;

import java.time.LocalTime;
import java.util.List;

public record PlaceResult(
        Long memberId,
        Long placeId,
        String placeName,
        PlaceCategory placeCategory,
        String categoryName,
        LocalTime endTime,
        DutyDto duty
) {
    public record DutyDto(
            Long dutyId,
            String dutyName,
            Integer totalCleaning,
            Integer endCleaning,
            List<CheckListDto> checkLists
    ) {
    }

    public record CheckListDto(
            Long checkListId,
            List<MemberDto> members,
            String cleaningName,
            LocalTime completeTime,
            Boolean needPhoto

    ) {
    }

    public record MemberDto(
            Long memberId,
            String memberName
    ) {
    }
}
