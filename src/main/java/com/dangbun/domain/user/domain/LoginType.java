package com.dangbun.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    EMAIL("email"),
    KAKAO("kakao");

    private final String value;
}
