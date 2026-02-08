package com.dangbun.domain.member.application.port.in.query;

public record MemberSearchResult(
        Long memberId,
        String name
) {
}
