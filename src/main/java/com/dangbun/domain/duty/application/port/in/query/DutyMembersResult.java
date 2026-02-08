package com.dangbun.domain.duty.application.port.in.query;

import java.util.List;

public record DutyMembersResult(List<MemberItem> members) {

    public record MemberItem(
            Long memberId,
            String role,
            String name
    ) {
    }

    public static DutyMembersResult of(List<MemberItem> members) {
        return new DutyMembersResult(members);
    }
}
