package com.dangbun.domain.place.refactor.adapter.in.web;

import io.swagger.v3.oas.annotations.media.Schema;

record PostCreatePlaceResponse(
        @Schema(description = "플레이스 Id", example = "1")
        Long placeId
){
    public static PostCreatePlaceResponse of(Long placeId){
        return new PostCreatePlaceResponse(placeId);
    }
}
