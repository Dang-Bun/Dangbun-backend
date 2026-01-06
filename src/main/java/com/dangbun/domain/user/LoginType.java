package com.dangbun.domain.user;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoginType {
    EMAIL("email"),
    KAKAO("kakao");

    private final String value;
}