package com.dangbun.domain.notification.dto.response;

import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetMemberSearchListResponse(
        @Schema(description = "멤버 검색 결과 목록")
        List<MemberDto> members,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
    public record MemberDto(
            @Schema(description = "멤버 ID", example = "1")
            Long memberId,

            @Schema(description = "멤버 이름", example = "멤버1")
            String memberName
    ) {
        public static MemberDto of(Member member) {
            return new MemberDto(member.getMemberId(), member.getName());
        }
    }

    public static GetMemberSearchListResponse of(List<MemberDto> members, boolean hasNext) {
        return new GetMemberSearchListResponse(members, hasNext);
    }
}
