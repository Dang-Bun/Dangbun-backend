package com.dangbun.domain.user.dto.request.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record PostUserLoginRequest (
        @Schema(example = "test@test.com")
        @Email @NotEmpty
        String email,
        @Schema(example = "testPassword")
        @NotEmpty
        String password
)implements LoginRequest {}
