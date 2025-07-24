package com.dangbun.domain.place.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

public record PostCheckInviteCodeRequest (
    @Schema(description = "참여코드", example = "abc123") String inviteCode
){}
