package com.dangbun.domain.duty.refactor.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

@Value
public class AddMembersCommand extends SelfValidating<AddMembersCommand> {

    @NotNull
    Long dutyId;

    @NotNull
    List<Long> memberIds;

    public AddMembersCommand(Long dutyId, List<Long> memberIds) {
        this.dutyId = dutyId;
        this.memberIds = memberIds;
        this.validateSelf();
    }
}
