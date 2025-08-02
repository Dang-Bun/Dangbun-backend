package com.dangbun.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record DeleteUserAccountRequest (
        @Schema(description = "이메일", example = "test@test.com")
        @Email @NotEmpty
        String email
){}
