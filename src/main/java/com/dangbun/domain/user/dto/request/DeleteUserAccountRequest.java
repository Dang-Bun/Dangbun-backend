package com.dangbun.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record DeleteUserAccountRequest (
    @Email @NotEmpty String email
){}
