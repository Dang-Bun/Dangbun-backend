package com.dangbun.domain.member.application.port.in.query;

import java.util.List;
import java.util.Map;

public record MemberDetailResult(
        MemberDto member,
        List<DutyDto> duties
) {
    public record MemberDto(
            String name,
            String role,
            Map<String, String> information
    ) {
    }

    public record DutyDto(
            Long dutyId,
            String dutyName
    ) {
    }
}
