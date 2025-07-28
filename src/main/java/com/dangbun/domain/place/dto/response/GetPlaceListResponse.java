package com.dangbun.domain.place.dto.response;

import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetPlaceListResponse(
        @Schema(description = "플레이스 리스트") List<PlaceDto> places
) {

    public static GetPlaceListResponse of(List<Member> memberList){
        List<PlaceDto> placeDtos = memberList.stream()
                .map(m ->
                    new PlaceDto(
                            m.getPlace().getPlaceId(),
                            m.getPlace().getName(),

                            0,
                            0,
                            m.getRole().getDisplayName(),
                            0
                    )
                ).toList();
        return new GetPlaceListResponse(placeDtos);
    }

    @Schema(name = "GetPlaceListResponse.PlaceDto", description = "플레이스 DTO")
    public record PlaceDto(
            @Schema(description = "플레이스 id", example = "1") Long placeId,
            @Schema(description = "플레이스 이름", example = "메가박스") String name,
            @Schema(description = "전체 청소", example = "10") int totalCleaning,
            @Schema(description = "완료된 청소", example = "7") int endCleaning,
            @Schema(description = "역할", example = "매니저") String role,
            @Schema(description = "새로운 알람", example = "4") int notifyNumber
    ) {
    }
}
