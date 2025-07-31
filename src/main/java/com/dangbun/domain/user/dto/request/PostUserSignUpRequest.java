package com.dangbun.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;

public record PostUserSignUpRequest(

        @Schema(example = "test@test.com")
        @Email @NotEmpty
        String email,
        @Schema(example = "1234")
        @NotEmpty
        String password,
        @Schema(description = "유저 이름", example = "홍길동")
        @NotEmpty
        String name,
        @Schema(description = "이메일 인증코드", example = "abc123")
        @NotEmpty
        String certCode
) {}