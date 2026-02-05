package com.dangbun.domain.duty.refactor.adapter.in.web.dto.request;

import com.dangbun.domain.duty.refactor.domain.DutyAssignType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.util.List;

import static com.dangbun.domain.duty.refactor.domain.DutyAssignType.*;

public record PatchAssignMemberRequest(
        @Schema(description = "멤버 할당 방식 (CUSTOM, COMMON, RANDOM 중에 필수 입력) ", example = "CUSTOM")
        @NotNull
        DutyAssignType assignType,

        @Schema(description = "역할 지정할 청소 ID (CUSTOM / COMMON 타입일 경우에만)", example = "1")
        Long cleaningId,

        @Schema(description = "해당 청소에 지정할 멤버 ID 리스트 (CUSTOM 타입일 경우에만)", example = "[1, 2, 3]")
        List<Long> memberIds,

        @Schema(description = "청소당 랜덤으로 배정할 멤버 수 (RANDOM 타입일 경우에만)", example = "2")
        Integer assignCount
) {
    @AssertTrue(message = "cleaningId는 null이 아니어야 합니다.")
    @Schema(hidden = true)
    public boolean isCustomFieldsValid() {
        if (assignType == CUSTOM) {
            return cleaningId != null;
        }
        return true;
    }

    @AssertTrue()
    @Schema(hidden = true)
    public boolean isCommonFieldsValid() {
        if (assignType == COMMON) {
            return cleaningId != null;
        }
        return true;
    }

    @AssertTrue()
    @Schema(hidden = true)
    public boolean isRandomFieldValid() {
        if (assignType == RANDOM) {
            return assignCount != null && assignCount > 0;
        }
        return true;
    }
}
