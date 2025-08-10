package com.dangbun.domain.place.dto.response;


import com.dangbun.domain.place.entity.PlaceCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetPlaceListResponse(
        @Schema(description = "플레이스 리스트") List<PlaceDto> places
) {



    public static GetPlaceListResponse of(List<PlaceDto> placeDtos) {
        return new GetPlaceListResponse(placeDtos);
    }

    @Schema(name = "GetPlaceListResponse.PlaceDto", description = "플레이스 DTO (참여 대기 중인 플레이스일 경우 placeId, name를 제외한 모든 필드 값 NULL)")
    public record PlaceDto(
            @Schema(description = "플레이스 id", example = "1") Long placeId,
            @Schema(description = "플레이스 이름", example = "메가박스") String name,
            @Schema(description = "플레이스 카테고리", example = "CAFE") PlaceCategory category,
            @Schema(description = "카테고리 이름", example = "카페") String categoryName,
            @Schema(description = "전체 청소", example = "10") Integer totalCleaning,
            @Schema(description = "완료된 청소", example = "7") Integer endCleaning,
            @Schema(description = "역할", example = "매니저") String role,
            @Schema(description = "새로운 알람", example = "4") Integer notifyNumber
    ) {
        public static PlaceDto of(Long placeId, String name, PlaceCategory category, String categoryName,Integer totalCleaning, Integer endCleaning, String role, Integer notifyNumber){
            return new PlaceDto(placeId, name, category, categoryName,totalCleaning, endCleaning, role, notifyNumber);
        }
    }
}
