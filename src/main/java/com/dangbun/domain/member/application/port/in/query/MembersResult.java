package com.dangbun.domain.member.application.port.in.query;

import java.util.List;

public record MembersResult(
        Integer waitingMemberNumber,
        List<MemberDto> members
) {
    public record MemberDto(
            Long memberId,
            String role,
            String name,
            List<String> dutyNames
    ) {
    }
}
