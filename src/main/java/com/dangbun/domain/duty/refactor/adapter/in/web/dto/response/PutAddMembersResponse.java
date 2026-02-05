package com.dangbun.domain.duty.refactor.adapter.in.web.dto.response;

import com.dangbun.domain.duty.refactor.application.port.in.query.AddMembersResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PutAddMembersResponse(
        @Schema(description = "추가된 멤버 ID 리스트", example = "[1, 2, 3]")
        List<Long> addedMemberIds
) {
    public static PutAddMembersResponse from(AddMembersResult result) {
        return new PutAddMembersResponse(result.addedMemberIds());
    }
}
