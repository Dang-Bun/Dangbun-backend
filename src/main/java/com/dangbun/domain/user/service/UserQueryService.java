package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.auth.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.dto.response.auth.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.DeleteMemberException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.jwt.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.INVALID_PASSWORD;
import static com.dangbun.global.security.jwt.TokenPrefix.ACCESS;
import static com.dangbun.global.security.jwt.TokenPrefix.REFRESH;

@RequiredArgsConstructor
@Service
public class UserQueryService {

    private final AuthCodeService authCodeService;
    private final UserRepository userRepository;
    private final AuthRedisService authRedisService;

    @Transactional(readOnly = true)
    public void sendFindPasswordAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isPresent()) {
            authCodeService.sendAuthCode(toEmail);
        } else{
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    public void logout(String bearerToken) {
        authRedisService.deleteAndSetBlacklist(bearerToken);
    }

    public GetUserMyInfoResponse getMyInfo(User user) {
        return GetUserMyInfoResponse.from(user);
    }

    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
