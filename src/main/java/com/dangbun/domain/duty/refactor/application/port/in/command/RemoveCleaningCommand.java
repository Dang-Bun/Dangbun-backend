package com.dangbun.domain.duty.refactor.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class RemoveCleaningCommand extends SelfValidating<RemoveCleaningCommand> {

    @NotNull
    Long dutyId;

    @NotNull
    Long cleaningId;

    public RemoveCleaningCommand(Long dutyId, Long cleaningId) {
        this.dutyId = dutyId;
        this.cleaningId = cleaningId;
        this.validateSelf();
    }
}
