package com.dangbun.domain.place.original.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostRegisterPlaceResponse (
        @Schema(description = "플레이스 ID" , example = "1")
        Long placeId
){
        public static PostRegisterPlaceResponse of(Long placeId){
                return new PostRegisterPlaceResponse(placeId);
        }
}
