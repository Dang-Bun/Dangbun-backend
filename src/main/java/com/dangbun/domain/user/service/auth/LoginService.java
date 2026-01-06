package com.dangbun.domain.user.service.auth;

import com.dangbun.domain.user.LoginType;
import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.response.auth.LoginResponse;
import com.dangbun.domain.user.service.auth.strategy.LoginProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class LoginService {
    private final List<LoginProvider> providers;

    public LoginResponse login(LoginType type, LoginRequest request) {
        return providers.stream()
                .filter(p -> p.supports(type))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 로그인 타입입니다."))
                .login(request);
    }
}