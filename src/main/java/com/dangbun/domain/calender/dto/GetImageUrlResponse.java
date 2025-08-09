package com.dangbun.domain.calender.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetImageUrlResponse(
        @Schema(description = "이미지 url")
        String imageUrl
) {
    public static GetImageUrlResponse of(String imageUrl){
        return new GetImageUrlResponse(imageUrl);
    }
}
