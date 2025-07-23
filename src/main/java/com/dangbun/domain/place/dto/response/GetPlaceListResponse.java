package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record GetPlaceListResponse (
        @NotNull @Schema(description = "플레이스 이름", example = "메가박스") String name,
        @Schema(description = "전체 청소",example = "10")  int total_cleaning,
        @Schema(description = "완료된 청소", example = "7") int end_cleaning,
        @NotNull @Schema(description = "역할", example = "매니저") String role,
        @Schema(description = "새로운 알람", example = "4") int notifyNumber
){

}
