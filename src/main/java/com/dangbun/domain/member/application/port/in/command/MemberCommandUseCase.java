package com.dangbun.domain.member.application.port.in.command;

import com.dangbun.common.hexagonal.UseCase;

@UseCase
public interface MemberCommandUseCase {

    void registerMember(Long memberId);

    void removeWaitingMember(Long memberId);

    void exitPlace(ExitPlaceCommand command);

    void removeMember(RemoveMemberCommand command);

    void assignDutyToMember(AssignDutyCommand command);
}
