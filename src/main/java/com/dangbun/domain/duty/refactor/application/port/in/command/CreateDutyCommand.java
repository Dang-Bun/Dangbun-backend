package com.dangbun.domain.duty.refactor.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import com.dangbun.domain.duty.refactor.domain.DutyIcon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

@Value
public class CreateDutyCommand extends SelfValidating<CreateDutyCommand> {

    @NotNull
    Long placeId;

    @NotBlank
    @Size(max = 20, message = "당번 이름은 20자 이하여야 합니다.")
    String name;

    @NotNull
    DutyIcon icon;

    public CreateDutyCommand(Long placeId, String name, DutyIcon icon) {
        this.placeId = placeId;
        this.name = name;
        this.icon = icon;
        this.validateSelf();
    }
}
