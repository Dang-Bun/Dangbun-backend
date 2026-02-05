package com.dangbun.domain.duty.refactor.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import com.dangbun.domain.duty.refactor.domain.DutyIcon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class UpdateDutyCommand extends SelfValidating<UpdateDutyCommand> {

    @NotNull
    Long dutyId;

    @NotBlank
    @Size(max = 20, message = "당번 이름은 20자 이하여야 합니다.")
    String name;

    @NotNull
    DutyIcon icon;

    public UpdateDutyCommand(Long dutyId, String name, DutyIcon icon) {
        this.dutyId = dutyId;
        this.name = name;
        this.icon = icon;
        this.validateSelf();
    }
}
