package com.dangbun.domain.member.adapter.in.web.dto.request;

import com.dangbun.domain.member.application.port.in.command.RemoveMemberCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RemoveMemberRequest(
        @Schema(description = "맴버 이름", example = "김철수")
        @NotBlank
        String memberName
) {
    public RemoveMemberCommand toCommand(Long memberId) {
        return new RemoveMemberCommand(memberId, memberName);
    }
}
