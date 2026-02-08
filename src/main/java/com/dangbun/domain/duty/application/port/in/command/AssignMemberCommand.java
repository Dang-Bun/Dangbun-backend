package com.dangbun.domain.duty.application.port.in.command;

import com.dangbun.common.hexagonal.SelfValidating;
import com.dangbun.domain.duty.domain.DutyAssignType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

import static com.dangbun.domain.duty.domain.DutyAssignType.*;

@Value
public class AssignMemberCommand extends SelfValidating<AssignMemberCommand> {

    @NotNull
    Long dutyId;

    @NotNull
    DutyAssignType assignType;

    Long cleaningId;

    List<Long> memberIds;

    Integer assignCount;

    public AssignMemberCommand(
            Long dutyId,
            DutyAssignType assignType,
            Long cleaningId,
            List<Long> memberIds,
            Integer assignCount
    ) {
        this.dutyId = dutyId;
        this.assignType = assignType;
        this.cleaningId = cleaningId;
        this.memberIds = memberIds;
        this.assignCount = assignCount;
        this.validateSelf();
    }

    @AssertTrue(message = "CUSTOM 타입일 때 cleaningId는 필수입니다.")
    public boolean isCustomFieldsValid() {
        if (assignType == CUSTOM) {
            return cleaningId != null;
        }
        return true;
    }

    @AssertTrue(message = "COMMON 타입일 때 cleaningId는 필수입니다.")
    public boolean isCommonFieldsValid() {
        if (assignType == COMMON) {
            return cleaningId != null;
        }
        return true;
    }

    @AssertTrue(message = "RANDOM 타입일 때 assignCount는 1 이상이어야 합니다.")
    public boolean isRandomFieldValid() {
        if (assignType == RANDOM) {
            return assignCount != null && assignCount > 0;
        }
        return true;
    }
}
