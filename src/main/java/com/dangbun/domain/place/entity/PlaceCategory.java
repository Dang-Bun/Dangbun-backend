package com.dangbun.domain.place.entity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PlaceCategory {
    CAFE("카페"),
    RESTAURANT("음식점"),
    THEATER("영화관"),
    DORMITORY("기숙사"),
    BUILDING("빌딩"),
    OFFICE("사무실"),
    SCHOOL("학교"),
    GYM("헬스장"),
    ETC("기타");

    private final String displayName;

    public String getDisplayName(){
        return displayName;
    }


}
