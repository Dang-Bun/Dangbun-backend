package com.dangbun.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record PostUserLoginRequest (
        @Email @NotEmpty String email,
        @NotEmpty String password
){}
