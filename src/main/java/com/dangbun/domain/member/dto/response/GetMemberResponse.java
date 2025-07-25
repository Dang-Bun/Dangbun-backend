package com.dangbun.domain.member.dto.response;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record GetMemberResponse(

        @Schema(description = "맴버 정보") MemberDto member,
        @Schema(description = "당번 정보(당번 Id, 당번 이름)") List<DutyDto> duties
) {

    public static GetMemberResponse of(Member member, List<Duty> duties) {
        List<DutyDto> dutyDtos = duties.stream()
                .map(d -> new DutyDto(
                        d.getDutyId(),
                        d.getName()
                )).toList();

        return new GetMemberResponse(
                new MemberDto(member.getName(),member.getRole().getDisplayName(), member.getInformation()),
                dutyDtos);
    }

    public record MemberDto(
            String name,
            String role,
            Map<String, String> information

    ) {
    }

    public record DutyDto(
            Long dutyId,
            String dutyName
    ) {
    }
}
