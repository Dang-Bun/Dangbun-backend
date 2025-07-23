package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCreateInviteCodeResponse (
        @Schema(description = "참여코드",example = "abc123") String inviteCode
){}
