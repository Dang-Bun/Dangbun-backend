package com.dangbun.domain.place.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

public record PostCheckInviteCodeResponse (
    @Schema(description = "참여코드", example = "AQ3536") String inviteCode,
    @Schema(description = "입력해야 되는 정보",example = "[\"이메일\",\"전화번호\"]") List<String> information
){
    public static PostCheckInviteCodeResponse of(String inviteCode, List<String> information){
        return new PostCheckInviteCodeResponse(inviteCode, information);
    }
}
