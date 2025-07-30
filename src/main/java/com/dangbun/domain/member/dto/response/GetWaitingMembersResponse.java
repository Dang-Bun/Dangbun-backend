package com.dangbun.domain.member.dto.response;

import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public record GetWaitingMembersResponse (
    @Schema(description = "맴버 목록") List<MemberDto> members
){
    public static GetWaitingMembersResponse of(List<Member> members){
        List<MemberDto> memberDtos = members.stream()
                .map( m ->new MemberDto(
                        m.getMemberId(),
                        m.getName(),
                        m.getInformation(),
                        m.getCreatedAt().toLocalDate()
                )).toList();
        return new GetWaitingMembersResponse(memberDtos);
    }

    @Schema(name = "GetWaitingMembersResponse.MemberDto", description = "맴버 DTO")
    public record MemberDto(
            @Schema(description = "맴버 ID", example = "1") Long memberId,
            @Schema(description = "이름", example = "홍길동") String name,
            @Schema(description = "맴버 정보", example = "{\"이메일\":\"gildong@test.com\",\"전화번호\":\"01012341234\"}")Map<String, String> information,
            @Schema(description = "맴버 생성 날짜", example = "20250731" ) LocalDate createdAt
            ){}
}
