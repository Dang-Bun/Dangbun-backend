package com.dangbun.domain.member.refactor.application.port.in.command;

public record AssignDutyCommand(
        Long memberId,
        Long dutyId
) {
}
