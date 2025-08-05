package com.dangbun.domain.checklist.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostSaveUploadResultRequest (
        @Schema(description = "S3 Key", example = "uploads/waefojaoh1324wedfo-example.jpg")
        String s3Key
){}
