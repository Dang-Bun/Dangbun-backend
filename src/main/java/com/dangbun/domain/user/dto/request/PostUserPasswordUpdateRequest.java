package com.dangbun.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record PostUserPasswordUpdateRequest(
    @Email @NotEmpty String email,
    @NotEmpty String certCode,
    @NotEmpty String password
){}
