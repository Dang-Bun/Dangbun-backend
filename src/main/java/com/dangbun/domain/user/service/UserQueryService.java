package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.PostUserLoginRequest;
import com.dangbun.domain.user.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.dto.response.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.DeleteMemberException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.security.refactor.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.INVALID_PASSWORD;
import static com.dangbun.global.security.refactor.TokenName.ACCESS;
import static com.dangbun.global.security.refactor.TokenName.REFRESH;

@RequiredArgsConstructor
@Service
public class UserQueryService {

    private final AuthCodeService authCodeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthRedisService authRedisService;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public void sendFindPasswordAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isPresent()) {
            authCodeService.sendAuthCode(toEmail);
        } else{
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }


    @Transactional(readOnly = true)
    public PostUserLoginResponse login(@Valid PostUserLoginRequest request) {


        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (!user.getEnabled()) {
            throw new DeleteMemberException(DELETE_MEMBER);
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        Map<String, String> tokenMap = jwtService.generateToken(user);
        authRedisService.saveRefreshToken(user.getUserId(), tokenMap.get(REFRESH.getName()));

        return new PostUserLoginResponse(tokenMap.get(ACCESS.getName()), tokenMap.get(REFRESH.getName()));
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
