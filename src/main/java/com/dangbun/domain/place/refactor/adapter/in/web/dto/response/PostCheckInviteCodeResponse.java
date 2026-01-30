package com.dangbun.domain.place.refactor.adapter.in.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record PostCheckInviteCodeResponse (
    @Schema(description = "플레이스 Id", example = "1") Long placeId,
    @Schema(description = "입력해야 되는 정보",example = "[\"이메일\",\"전화번호\"]") List<String> information
){
    public static PostCheckInviteCodeResponse of(Long placeId, List<String> information){
        return new PostCheckInviteCodeResponse(placeId, information);
    }
}
