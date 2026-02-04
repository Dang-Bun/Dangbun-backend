package com.dangbun.domain.member.refactor.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    MANAGER("매니저"),
    MEMBER("멤버"),
    WAITING("대기중")
    ;

    private final String displayName;
}
