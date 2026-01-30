package com.dangbun.domain.place.refactor.application.port.in.query;

import com.dangbun.domain.place.refactor.domain.PlaceCategory;

import java.util.List;

public record PlaceListResult(
        List<PlaceDto> places
) {
    public record PlaceDto(
            Long placeId,
            String name,
            PlaceCategory category,
            String categoryName,
            Integer totalCleaning,
            Integer endCleaning,
            String role,
            Integer notifyNumber
    ) {
    }
}
