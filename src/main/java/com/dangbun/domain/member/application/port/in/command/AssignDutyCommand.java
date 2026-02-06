package com.dangbun.domain.member.application.port.in.command;

public record AssignDutyCommand(
        Long memberId,
        Long dutyId
) {
}
