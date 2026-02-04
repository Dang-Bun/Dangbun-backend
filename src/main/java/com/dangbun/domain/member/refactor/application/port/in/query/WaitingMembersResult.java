package com.dangbun.domain.member.refactor.application.port.in.query;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public record WaitingMembersResult(
        List<MemberDto> members
) {
    public record MemberDto(
            Long memberId,
            String name,
            Map<String, String> information,
            LocalDate createdAt
    ) {
    }
}
