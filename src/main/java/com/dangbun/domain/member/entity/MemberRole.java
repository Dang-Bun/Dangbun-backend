package com.dangbun.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    MANAGER("매니저"),
    MEMBER("멤버")
    ;

    private final String displayName;
}
