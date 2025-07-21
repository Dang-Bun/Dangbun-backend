package com.dangbun.domain.duty.dto.response;

import java.util.List;

public record PostAddMembersResponse(
        List<Long> addedMemberId
) {
    public static PostAddMembersResponse of(List<Long> addedMemberId) {
        return new PostAddMembersResponse(addedMemberId);
    }
}