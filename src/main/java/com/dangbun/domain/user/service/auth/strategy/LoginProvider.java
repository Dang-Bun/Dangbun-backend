package com.dangbun.domain.user.service.auth.strategy;

import com.dangbun.domain.user.LoginType;
import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.response.auth.LoginResponse;
import jakarta.validation.Valid;

public interface LoginProvider {

    boolean supports(LoginType type);

    LoginResponse login(@Valid LoginRequest request);
}
