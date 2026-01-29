package com.dangbun.domain.place.refactor.adapter.in.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record PostCheckInviteCodeRequest(
        @Schema(description = "참여코드", example = "abc123")
        @NotBlank
        String inviteCode
) {
}
