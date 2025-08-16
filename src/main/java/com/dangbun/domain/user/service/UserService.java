package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.DeleteUserAccountRequest;
import com.dangbun.domain.user.dto.request.PostUserLoginRequest;
import com.dangbun.domain.user.dto.request.PostUserPasswordUpdateRequest;
import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.dto.response.PostUserLoginResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.*;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.AuthRedisService;
import com.dangbun.global.redis.RedisService;
import com.dangbun.global.security.JwtUtil;
import com.dangbun.global.security.TokenProvider;
import com.dangbun.global.security.refactor.JwtService;
import com.dangbun.global.security.refactor.TokenName;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;
import static com.dangbun.global.security.refactor.TokenName.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final String CERT_CODE_PREFIX = "AuthCode";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final AuthRedisService authRedisService;
    private final JwtService jwtService;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;


    @Transactional(readOnly = true)
    public void sendFindPasswordAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isPresent()) {
            sendAuthCode(toEmail);
        } else{
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    @Transactional
    public void sendSignupAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isEmpty()) {
            sendAuthCode(toEmail);
            return;
        }

        User user = getUserByEmail(toEmail).get();
        if(!user.getEnabled()){
            userRepository.delete(user);
            sendAuthCode(toEmail);
            return;
        }
        throw new ExistEmailException(EXIST_EMAIL);
    }


    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private String createAuthCode() {
        int length = 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException();
        }
    }

    @Transactional
    public void signup(@Valid PostUserSignUpRequest request) {

        String name = request.name();
        String rawPassword = request.password();
        String email = request.email();
        String certCode = request.certCode();


        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ExistEmailException(EXIST_EMAIL);
        }

        checkCertCode(email, certCode);

        if (!isValidPassword(rawPassword)) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        String encodePassword = passwordEncoder.encode(rawPassword);

        User user = User.builder().
                name(name)
                .enabled(true)
                .password(encodePassword)
                .email(email)
                .build();

        userRepository.save(user);
    }

    private void checkCertCode(String email, String certCode) {
        if (!redisService.getValues(CERT_CODE_PREFIX + email).equals(certCode)) {
            throw new InvalidCertCodeException(INVALID_CERT_CODE);
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }

    @Transactional
    public void updatePassword(@Valid PostUserPasswordUpdateRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        checkCertCode(request.email(), request.certCode());

        if (isValidPassword(request.password())) {
            String encodedPassword = passwordEncoder.encode(request.password());
            user.updatePassword(encodedPassword);
        }

        userRepository.save(user);

    }

    @Transactional
    public void deleteCurrentUser(User user, DeleteUserAccountRequest request) {
        if (request.email() != null && user.getEmail().equals(request.email())) {
            user.deactivate();
            userRepository.save(user);
            return;
        }
        throw new InvalidEmailException(INVALID_EMAIL);
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


    private void sendAuthCode(String toEmail) {
        String title = "당번 이메일 인증 번호";
        String certCode = createAuthCode();
        emailService.sendEmail(toEmail, title, certCode);
        redisService.setValues(CERT_CODE_PREFIX + toEmail, certCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }
}
