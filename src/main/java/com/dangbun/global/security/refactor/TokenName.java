package com.dangbun.global.security.refactor;

import lombok.Getter;

@Getter
public enum TokenName {
    ACCESS("accessToken"),

    REFRESH("refreshToken");

    private final String name;

    TokenName(String name){
        this.name = name;
    }
}
