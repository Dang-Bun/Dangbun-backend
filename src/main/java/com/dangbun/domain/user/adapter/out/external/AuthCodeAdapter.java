package com.dangbun.domain.user.adapter.out.external;

import com.dangbun.domain.user.application.port.out.AuthCodePort;
import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.AuthRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Random;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.INVALID_CERT_CODE;


@Component
@RequiredArgsConstructor
public class AuthCodeAdapter implements AuthCodePort {

    private final EmailService emailService;
    private final AuthRedisService authRedisService;

    private static final String AUTH_CODE_PREFIX = "AuthCode";

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    @Override
    public void sendAuthCode(String email) {
        String title = "당번 이메일 인증 번호";
        String authCode = createAuthCode();
        authRedisService.checkDuration(AUTH_CODE_PREFIX + email, Duration.ofMillis(this.authCodeExpirationMillis));
        emailService.sendEmail(email, title, authCode);
        authRedisService.saveAuthCode(AUTH_CODE_PREFIX + email, authCode, Duration.ofMillis(this.authCodeExpirationMillis));
    }

    @Override
    public void checkAuthCode(String email, String authCode) {
        String savedCode = authRedisService.getAuthCode(AUTH_CODE_PREFIX + email);
        if (savedCode == null || !savedCode.equals(authCode)) {
            throw new InvalidCertCodeException(INVALID_CERT_CODE);
        }
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
            throw new RuntimeException("인증 코드 생성 실패", e);
        }
    }
}
