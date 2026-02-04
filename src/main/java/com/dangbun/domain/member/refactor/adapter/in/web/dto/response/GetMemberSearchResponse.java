package com.dangbun.domain.member.refactor.adapter.in.web.dto.response;

import com.dangbun.domain.member.refactor.application.port.in.query.MemberSearchResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetMemberSearchResponse(
        @Schema(description = "검색 결과 멤버 ID", example = "1")
        Long memberId,
        @Schema(description = "검색 결과 멤버 이름", example = "박완")
        String name
) {
    public static GetMemberSearchResponse from(MemberSearchResult result) {
        return new GetMemberSearchResponse(result.memberId(), result.name());
    }
}
