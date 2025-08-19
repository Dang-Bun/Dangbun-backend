package com.dangbun.domain.duty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PutAddMembersResponse(
        @Schema(description = "지정된 멤버 ID 목록")
        List<Long> addedMemberIds
) {
    public static PutAddMembersResponse of(List<Long> addedMemberIds) {
        return new PutAddMembersResponse(addedMemberIds);
    }
}