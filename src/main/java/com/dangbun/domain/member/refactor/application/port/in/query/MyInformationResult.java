package com.dangbun.domain.member.refactor.application.port.in.query;

public record MyInformationResult(
        Long memberId,
        String memberName,
        String memberRole
) {
}
