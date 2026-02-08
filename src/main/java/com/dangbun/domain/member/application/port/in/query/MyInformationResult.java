package com.dangbun.domain.member.application.port.in.query;

public record MyInformationResult(
        Long memberId,
        String memberName,
        String memberRole
) {
}
