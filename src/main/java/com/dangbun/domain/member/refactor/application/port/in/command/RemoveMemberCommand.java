package com.dangbun.domain.member.refactor.application.port.in.command;

public record RemoveMemberCommand(
        Long memberId,
        String memberName
) {
}
