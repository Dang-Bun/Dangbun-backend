package com.dangbun.domain.member.dto.response;

import java.util.List;
import java.util.Map;

public record GetMemberResponse(

) {
    public record MemberDto(
            String name,
            String role,
            Duty duty,
            Map<String, String> information

            ) {
    }

    public record Duty(
        List<String> dutyName
    ) {
    }
}
