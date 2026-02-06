package com.dangbun.domain.member.adapter.in.web.dto.response;

import com.dangbun.domain.member.application.port.in.query.MembersResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetMembersResponse(
        @Schema(description = "대기 맴버 숫자", example = "4") Integer waitingMemberNumber,
        @Schema(description = "맴버 리스트") List<MemberDto> members
) {
    public static GetMembersResponse from(MembersResult result) {
        List<MemberDto> memberDtos = result.members().stream()
                .map(m -> new MemberDto(
                        m.memberId(),
                        m.role(),
                        m.name(),
                        m.dutyNames()
                )).toList();
        return new GetMembersResponse(result.waitingMemberNumber(), memberDtos);
    }

    @Schema(name = "GetMembersResponse.MemberDto", description = "맴버 DTO")
    public record MemberDto(
            @Schema(description = "맴버 ID", example = "1")
            Long memberId,
            @Schema(description = "맴버 역할", example = "멤버")
            String role,
            @Schema(description = "맴버 이름", example = "홍길동")
            String name,
            @Schema(description = "당번 이름", example = "[\"화장실 청소\", \"바닥 청소\"]")
            List<String> dutyName
    ) {
    }
}
