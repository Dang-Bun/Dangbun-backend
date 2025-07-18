package com.dangbun.domain.duty.dto.request;

import com.dangbun.domain.duty.entity.DutyIcon;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PostDutyCreateRequest(
        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        @NotBlank()
        @Size(max = 20, message = "당번 이름은 20자 이하여야 합니다.")
        String name,

        @Schema(description = "아이콘 코드", example = "BUCKET_PINK")
        @NotNull()
        DutyIcon icon
) {
}
