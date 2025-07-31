package com.dangbun.domain.duty.dto.response;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetDutyMemberNameListResponse(
        @Schema(description = "멤버 ID", example = "1")
        Long memberId,
        @Schema(description = "멤버 역할", example = "매니저")
        MemberRole role,
        @Schema(description = "멤버 이름", example = "박완")
        String name
) {
        public static GetDutyMemberNameListResponse of(Member member) {
            return new GetDutyMemberNameListResponse(member.getMemberId(), member.getRole(), member.getName());
        }
}
