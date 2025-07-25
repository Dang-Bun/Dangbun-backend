package com.dangbun.domain.member.dto.response;

import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record GetWaitingMembersResponse (
    List<MemberDto> member
){
    public static GetWaitingMembersResponse of(List<Member> members){
        List<MemberDto> member = members.stream()
                .map( m ->new MemberDto(
                        m.getMemberId(),
                        m.getName(),
                        m.getInformation()
                )).toList();
        return new GetWaitingMembersResponse(member);
    }

    public record MemberDto(
            @Schema(description = "맴버 Id") Long memberId,
            @Schema(description = "이름") String name,
            @Schema(description = "정보")Map<String, String> information
            ){}
}
