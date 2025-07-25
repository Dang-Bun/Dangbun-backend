package com.dangbun.domain.member.dto.response;


import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record GetMembersResponse (
        @Schema(description = "맴버", example = "매니저, 김철수, 탕비실 청소 당번") List<MemberDto> members
){
    public static GetMembersResponse of(Map<Member,List<String>> memberMap){
        List<MemberDto> memberDtos = memberMap.entrySet().stream()
                .map(entry -> new MemberDto(
                        entry.getKey().getMemberId(),
                        entry.getKey().getRole().getDisplayName(),
                        entry.getKey().getName(),
                        entry.getValue()
                )).toList();
        return new GetMembersResponse(memberDtos);
    }

    public record MemberDto(
            Long memberId,
            String role,
            String name,
            List<String> dutyName
    ){}

}
