package com.dangbun.domain.member.adapter.in.web.dto.response;

import com.dangbun.domain.member.application.port.in.query.MyInformationResult;
import io.swagger.v3.oas.annotations.media.Schema;

public record GetMyInformationResponse(
        @Schema(description = "아이디", example = "1") Long memberId,
        @Schema(description = "맴버 이름", example = "홍길동") String memberName,
        @Schema(description = "역할", example = "매니저") String memberRole
) {
    public static GetMyInformationResponse from(MyInformationResult result) {
        return new GetMyInformationResponse(
                result.memberId(),
                result.memberName(),
                result.memberRole()
        );
    }
}
