package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCreatePlaceResponse (
        @Schema(description = "플레이스 Id", example = "1")
        Long placeId
){
    public static PostCreatePlaceResponse of(Long placeId){
        return new PostCreatePlaceResponse(placeId);
    }
}
