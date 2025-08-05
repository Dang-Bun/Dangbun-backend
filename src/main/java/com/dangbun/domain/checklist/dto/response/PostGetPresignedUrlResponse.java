package com.dangbun.domain.checklist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record PostGetPresignedUrlResponse (
        @Schema(description = "이미지 업로드 용 url" ,example = "https://my-bucket.s3.amazonaws.com/uploads/abc.jpg?X-Amz-Algorithm=AWS4-HMAC-SHA256&X-Amz-Credential=...")
        String uploadUrl,
        @Schema(description = "S3 Key", example = "uploads/waefojaoh1324wedfo-example.jpg")
        String s3Key
){}
