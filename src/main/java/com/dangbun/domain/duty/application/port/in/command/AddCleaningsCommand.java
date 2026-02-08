package com.dangbun.domain.duty.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

@Value
public class AddCleaningsCommand extends SelfValidating<AddCleaningsCommand> {

    @NotNull
    Long dutyId;

    @NotNull
    List<Long> cleaningIds;

    public AddCleaningsCommand(Long dutyId, List<Long> cleaningIds) {
        this.dutyId = dutyId;
        this.cleaningIds = cleaningIds;
        this.validateSelf();
    }
}
