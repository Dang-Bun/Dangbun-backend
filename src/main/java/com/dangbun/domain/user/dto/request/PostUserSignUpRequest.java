package com.dangbun.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;

public record PostUserSignUpRequest(

        @Email @NotEmpty String email,
        @NotEmpty
        String password,
        @NotEmpty
        String name,
        @NotEmpty
        String certCode
) {}