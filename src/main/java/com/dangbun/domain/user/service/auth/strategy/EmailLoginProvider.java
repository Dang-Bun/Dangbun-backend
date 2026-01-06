package com.dangbun.domain.user.service.auth.strategy;

import com.dangbun.domain.user.entity.LoginType;
import com.dangbun.domain.user.dto.request.auth.LoginRequest;
import com.dangbun.domain.user.dto.request.auth.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.dto.response.auth.LoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.DeleteMemberException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;
import static com.dangbun.global.security.jwt.TokenPrefix.ACCESS;
import static com.dangbun.global.security.jwt.TokenPrefix.REFRESH;

@RequiredArgsConstructor
@Component
public class EmailLoginProvider implements LoginProvider{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRedisService authRedisService;
    private final JwtService jwtService;

    @Override
    public boolean supports(LoginType type) {
        return type.equals(LoginType.EMAIL);
    }

    @Override
    public LoginResponse login(LoginRequest req) {

        if (!(req instanceof PostUserLoginRequest request)) {
            throw new IllegalArgumentException("Invalid login request type");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (!user.getEnabled()) {
            throw new DeleteMemberException(DELETE_MEMBER);
        }

        if(!user.getLoginType().equals(LoginType.EMAIL)){
            throw new NoSuchUserException(NO_SUCH_USER);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        Map<String, String> tokenMap = jwtService.generateToken(user);
        authRedisService.saveRefreshToken(user.getUserId(), tokenMap.get(REFRESH.getName()));

        return new PostUserLoginResponse(tokenMap.get(ACCESS.getName()), tokenMap.get(REFRESH.getName()));
    }
}
