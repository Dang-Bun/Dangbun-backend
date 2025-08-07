package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "GetDutiesProgressResponse.DutyDto")
public record DutyProgressDto(
        @Schema(description = "당번 Id", example = "1")
        Long dutyId,

        @Schema(description = "당번 이름", example = "메가박스")
        String dutyName,

        @Schema(description = "전체 청소 수", example = "10")
        Long totalCleaning,

        @Schema(description = "완료된 청소 수", example = "3")
        Long endCleaning
) {
    public static DutyProgressDto of(Long dutyId, String dutyName, Long totalCleaning, Long endCleaning) {
        return new DutyProgressDto(dutyId, dutyName, totalCleaning, endCleaning);
    }
}
