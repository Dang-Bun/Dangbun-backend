package com.dangbun.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record DeleteSelfFromPlaceRequest(
        @Schema(description = "플레이스 이름", example = "메가박스")
        @NotBlank
        String placeName
) {
}
