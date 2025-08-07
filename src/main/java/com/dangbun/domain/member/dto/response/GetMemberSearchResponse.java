package com.dangbun.domain.member.dto.response;

import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetMemberSearchResponse(
        @Schema(description = "검색 결과 멤버 ID", example = "1")
        Long memberId,
        @Schema(description = "검색 결과 멤버 이름", example = "박완")
        String name
) {
    public static GetMemberSearchResponse of(Member member) {
        return new GetMemberSearchResponse(member.getMemberId(), member.getName());
    }
}
