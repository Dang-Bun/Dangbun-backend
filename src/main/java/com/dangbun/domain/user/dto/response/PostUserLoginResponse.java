package com.dangbun.domain.user.dto.response;

import jakarta.validation.constraints.NotEmpty;

public record PostUserLoginResponse (
        @NotEmpty String accessToken,
        @NotEmpty String refreshToken
){}
