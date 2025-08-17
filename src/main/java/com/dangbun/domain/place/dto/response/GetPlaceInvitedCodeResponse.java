package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GetPlaceInvitedCodeResponse(
        @Schema(description = "플레이스 초대 코드", example = "ABCDEF")
        String inviteCode
) {
        public static GetPlaceInvitedCodeResponse of(String code) {
                return new GetPlaceInvitedCodeResponse(code);
        }
}
