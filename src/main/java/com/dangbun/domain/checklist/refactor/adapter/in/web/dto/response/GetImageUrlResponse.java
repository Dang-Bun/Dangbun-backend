package com.dangbun.domain.checklist.refactor.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetImageUrlResponse (
        @Schema(description = "이미지 확인 url")
        String accessUrl
){}
