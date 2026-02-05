package com.dangbun.domain.duty.refactor.adapter.in.web.dto.response;

import com.dangbun.domain.duty.refactor.application.port.in.query.DutyMembersResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyMemberNameListResponse(
        @Schema(description = "멤버 ID", example = "1")
        Long memberId,
        @Schema(description = "멤버 역할", example = "MANAGER")
        String role,
        @Schema(description = "멤버 이름", example = "박완")
        String name
) {
    public static GetDutyMemberNameListResponse from(DutyMembersResult.MemberItem item) {
        return new GetDutyMemberNameListResponse(
                item.memberId(),
                item.role(),
                item.name()
        );
    }
}
