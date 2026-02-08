package com.dangbun.domain.user.adapter.in.web.dto.response.auth;

import jakarta.validation.constraints.NotEmpty;

public record PostUserLoginResponse (
        @NotEmpty String accessToken,
        @NotEmpty String refreshToken
)implements LoginResponse {}
