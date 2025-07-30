package com.dangbun.domain.place.dto.response;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.place.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

public record GetPlaceListResponse(
        @Schema(description = "플레이스 리스트") List<PlaceDto> places
) {

    public static GetPlaceListResponse of(List<Member> memberList){
        List<PlaceDto> placeDtos = new ArrayList<>();
        for(Member member : memberList){
            if(!member.getStatus()){
                Place place = member.getPlace();
                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(), null, null, null, null));
            }else {
                Place place = member.getPlace();
                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(), 0,0,member.getRole().getDisplayName(), 0));
            }
        }
        return new GetPlaceListResponse(placeDtos);
    }

    @Schema(name = "GetPlaceListResponse.PlaceDto", description = "플레이스 DTO")
    public record PlaceDto(
            @Schema(description = "플레이스 id", example = "1") Long placeId,
            @Schema(description = "플레이스 이름", example = "메가박스") String name,
            @Schema(description = "전체 청소", example = "10") Integer totalCleaning,
            @Schema(description = "완료된 청소", example = "7") Integer endCleaning,
            @Schema(description = "역할", example = "매니저") String role,
            @Schema(description = "새로운 알람", example = "4") Integer notifyNumber
    ) {
        public static PlaceDto of(Long placeId, String name, Integer totalCleaning, Integer endCleaning, String role, Integer notifyNumber){
            return new PlaceDto(placeId, name, totalCleaning, endCleaning, role, notifyNumber);
        }
    }
}
