package com.dangbun.domain.user.dto.request;

public record PostUserSignUpRequest(
        String email,
        String password,
        String name
) {}