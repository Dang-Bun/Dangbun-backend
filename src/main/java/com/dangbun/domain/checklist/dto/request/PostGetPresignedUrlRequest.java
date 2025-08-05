package com.dangbun.domain.checklist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PostGetPresignedUrlRequest(
        @Schema(description = "파일명", example = "example.jpg")
        @NotBlank
        String originalFileName,

        @Schema(description = "콘텐츠 타입", example = "image/jpeg")
        @NotBlank
        String contentType
) {
}
