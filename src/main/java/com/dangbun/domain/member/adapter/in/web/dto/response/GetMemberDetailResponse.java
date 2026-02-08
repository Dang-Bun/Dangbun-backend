package com.dangbun.domain.member.adapter.in.web.dto.response;

import com.dangbun.domain.member.application.port.in.query.MemberDetailResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

public record GetMemberDetailResponse(
        @Schema(description = "맴버 정보") MemberDto member,
        @Schema(description = "당번 정보(당번 Id, 당번 이름)") List<DutyDto> duties
) {
    public static GetMemberDetailResponse from(MemberDetailResult result) {
        MemberDto memberDto = new MemberDto(
                result.member().name(),
                result.member().role(),
                result.member().information()
        );

        List<DutyDto> dutyDtos = result.duties().stream()
                .map(d -> new DutyDto(d.dutyId(), d.dutyName()))
                .toList();

        return new GetMemberDetailResponse(memberDto, dutyDtos);
    }

    @Schema(name = "GetMemberDetailResponse.MemberDto", description = "맴버 DTO")
    public record MemberDto(
            @Schema(description = "맴버 이름", example = "홍길동")
            String name,
            @Schema(description = "맴버 역할", example = "MANAGER")
            String role,
            @Schema(description = "맴버 정보", example = "{\"이메일\":\"gildong@test.com\",\"전화번호\":\"01012341234\"}")
            Map<String, String> information
    ) {
    }

    @Schema(description = "당번 DTO")
    public record DutyDto(
            @Schema(description = "당번 id", example = "1")
            Long dutyId,
            @Schema(description = "당번 이름", example = "메가박스")
            String dutyName
    ) {
    }
}
