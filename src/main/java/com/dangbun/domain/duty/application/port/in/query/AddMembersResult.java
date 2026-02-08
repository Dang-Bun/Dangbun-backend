package com.dangbun.domain.duty.application.port.in.query;

import java.util.List;

public record AddMembersResult(List<Long> addedMemberIds) {

    public static AddMembersResult of(List<Long> addedMemberIds) {
        return new AddMembersResult(addedMemberIds);
    }
}
