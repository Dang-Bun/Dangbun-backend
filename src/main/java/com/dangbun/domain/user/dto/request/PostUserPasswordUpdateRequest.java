package com.dangbun.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record PostUserPasswordUpdateRequest(
    @Schema(example = "test@test.com")
    @Email @NotEmpty
    String email,
    @Schema(description = "이메일 인증코드", example = "abc123")
    @NotEmpty
    String certCode,
    @Schema(description = "비밀번호", example = "1234")
    @NotEmpty
    String password
){}
