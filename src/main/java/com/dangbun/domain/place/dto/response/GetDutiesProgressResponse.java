package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetDutiesProgressResponse(
        List<DutyDto> dutyDtos
) {

    public static GetDutiesProgressResponse of(List<DutyDto> dutyDtos){
        return new GetDutiesProgressResponse(dutyDtos);
    }

    @Schema(name = "GetDutiesProgressResponse.DutyDto")
    public record DutyDto(
            @Schema(description = "당번 Id",example = "1")
            Long dutyId,

            @Schema(description = "당번 이름",example = "메가박스")
            String dutyName,

            @Schema(description = "전체 청소 수", example = "10")
            Long totalCleaning,

            @Schema(description = "완료된 청소 수",example = "3")
            Long endCleaning
    ){
        public static DutyDto of(Long dutyId, String dutyName, Long totalCleaning, Long endCleaning){
            return new DutyDto(dutyId, dutyName, totalCleaning, endCleaning);
        }
    }
}
