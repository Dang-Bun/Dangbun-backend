package com.dangbun.global.security.jwt;

import lombok.Getter;

@Getter
public enum TokenPrefix {
    ACCESS("accessToken"),

    REFRESH("refreshToken"),

    BEARER("Bearer ");

    private final String name;

    TokenPrefix(String name){
        this.name = name;
    }
}
