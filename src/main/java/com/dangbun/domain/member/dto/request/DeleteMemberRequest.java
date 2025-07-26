package com.dangbun.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record DeleteMemberRequest (
        @Schema(description = "맴버 이름", example = "김철수") String memberName
){}
