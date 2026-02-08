package com.dangbun.domain.duty.application.port.in.command;

import com.dangbun.domain.duty.application.port.in.query.AddCleaningsResult;
import com.dangbun.domain.duty.application.port.in.query.AddMembersResult;
import com.dangbun.domain.duty.application.port.in.query.UpdateDutyResult;

public interface DutyCommandUseCase {

    Long createDuty(CreateDutyCommand command);

    UpdateDutyResult updateDuty(UpdateDutyCommand command);

    void deleteDuty(Long dutyId);

    AddMembersResult addMembers(AddMembersCommand command);

    void assignMember(AssignMemberCommand command);

    AddCleaningsResult addCleanings(AddCleaningsCommand command);

    void removeCleaning(RemoveCleaningCommand command);
}
