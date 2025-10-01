package com.dangbun.global.security.jwt;

import lombok.Getter;

@Getter
public enum TokenAge {
    //  3 hours
    ACCESS(1000L * 60 * 60 * 3),

    //  15 days
    REFRESH(1000L * 60 * 60 * 24 * 15);

    private final long age;

    TokenAge(long age){
        this.age = age;
    }
}
