package com.dangbun.domain.member.application.port.in.command;

public record RemoveMemberCommand(
        Long memberId,
        String memberName
) {
}
