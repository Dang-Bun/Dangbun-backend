package com.dangbun.domain.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetRecentSearchResponse (
        @Schema(description = "최근 검색어 목록", example =  "[\"멤버1\", \"멤\"]")
        List<String> recentSearch
){
    public static GetRecentSearchResponse of(List<String> recentSearch) {
        return new GetRecentSearchResponse(recentSearch);
    }
}

