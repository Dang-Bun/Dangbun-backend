package com.dangbun.domain.user.service;

import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.domain.user.verify.AuthCodeGenerator;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class VerificationService {


    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final AuthCodeGenerator authCodeGenerator;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    private static final String CERT_CODE_PREFIX = "AuthCode";

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public void sendFindPasswordAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isPresent()) {
            sendAuthCode(toEmail);
        } else{
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    public void checkCertCode(String email, String certCode) {
        if (!redisService.getValues(CERT_CODE_PREFIX + email).equals(certCode)) {
            throw new InvalidCertCodeException(INVALID_CERT_CODE);
        }
    }

    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private void sendAuthCode(String toEmail) {
        String title = "당번 이메일 인증 번호";
        String certCode = authCodeGenerator.createAuthCode();
        emailService.sendEmail(toEmail, title, certCode);
        redisService.setValues(CERT_CODE_PREFIX + toEmail, certCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }


}
