package com.dangbun.domain.duty.dto.response;

import com.dangbun.domain.duty.entity.DutyIcon;
import com.dangbun.domain.member.entity.MemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GetDutyInfoResponse(
        @Schema(description = "당번 ID", example = "1")
        Long dutyId,
        @Schema(description = "당번 이름", example = "탕비실 청소 당번")
        String name,
        @Schema(description = "아이콘 코드", example = "BUCKET_PINK")
        DutyIcon icon,
        @Schema(description = "당번의 멤버 목록")
        List<MemberDto> members,
        @Schema(description = "당번의 청소 목록")
        List<CleaningDto> cleanings
) {
    public static GetDutyInfoResponse of(Long dutyId, String name, DutyIcon icon,
                                           List<MemberDto> members, List<CleaningDto> cleanings) {
        return new GetDutyInfoResponse(dutyId, name, icon, members, cleanings);
    }

    public record MemberDto(
            @Schema(description = "멤버 ID", example = "1")
            Long memberId,
            @Schema(description = "멤버 역할", example = "매니저")
            MemberRole role,
            @Schema(description = "멤버 이름", example = "박완")
            String name
    ) {}

    public record CleaningDto(
            @Schema(description = "청소 ID", example = "1")
            Long cleaningId,
            @Schema(description = "청소 이름", example = "바닥 쓸기")
            String name
    ) {}
}
