package com.dangbun.domain.duty.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostAddMembersResponse(
        @Schema(description = "추가된 멤버 ID 목록")
        List<Long> addedMemberId
) {
    public static PostAddMembersResponse of(List<Long> addedMemberId) {
        return new PostAddMembersResponse(addedMemberId);
    }
}