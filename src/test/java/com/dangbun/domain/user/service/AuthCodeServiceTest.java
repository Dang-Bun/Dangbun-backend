package com.dangbun.domain.user.service;

import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.AuthRedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AuthCodeServiceTest {

    @InjectMocks
    private AuthCodeService authCodeService;

    @Mock
    private EmailService emailService;
    @Mock
    private AuthRedisService authRedisService;

    @BeforeEach
    void setUp() {
        // authCodeExpirationMillis 값 설정
        ReflectionTestUtils.setField(authCodeService, "authCodeExpirationMillis", 300000L); // 5분
    }

    @Test
    @DisplayName("인증코드 전송 - 성공")
    void sendAuthCode_success() {
        // given
        String email = "test@test.com";
        doNothing().when(authRedisService).checkDuration(anyString(), any(Duration.class));

        // when
        authCodeService.sendAuthCode(email);

        // then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> authCodeCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);

        then(authRedisService).should().checkDuration(
                eq("AuthCodetest@test.com"), 
                eq(Duration.ofMillis(300000L))
        );
        
        then(emailService).should().sendEmail(
                eq(email), 
                eq("당번 이메일 인증 번호"), 
                authCodeCaptor.capture()
        );
        
        then(authRedisService).should().saveAuthCode(
                keyCaptor.capture(), 
                anyString(), 
                durationCaptor.capture()
        );

        // 인증코드 형식 검증 (6자리 숫자)
        String capturedAuthCode = authCodeCaptor.getValue();
        assertThat(capturedAuthCode).hasSize(6);
        assertThat(capturedAuthCode).matches("\\d{6}");
        
        // Redis 키 검증
        assertThat(keyCaptor.getValue()).isEqualTo("AuthCodetest@test.com");
        
        // Duration 검증
        assertThat(durationCaptor.getValue()).isEqualTo(Duration.ofMillis(300000L));
    }

    @Test
    @DisplayName("인증코드 검증 - 성공")
    void checkAuthCode_success() {
        // given
        String email = "test@test.com";
        String authCode = "123456";
        
        given(authRedisService.getAuthCode("AuthCodetest@test.com")).willReturn("123456");

        // when & then
        assertThatCode(() -> authCodeService.checkAuthCode(email, authCode))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("인증코드 검증 - 불일치")
    void checkAuthCode_mismatch() {
        // given
        String email = "test@test.com";
        String authCode = "654321";
        
        given(authRedisService.getAuthCode("AuthCodetest@test.com")).willReturn("123456");

        // when & then
        assertThatThrownBy(() -> authCodeService.checkAuthCode(email, authCode))
                .isInstanceOf(InvalidCertCodeException.class);
    }

    @Test
    @DisplayName("인증코드 검증 - Redis에 코드 없음")
    void checkAuthCode_notFound() {
        // given
        String email = "test@test.com";
        String authCode = "123456";
        
        given(authRedisService.getAuthCode("AuthCodetest@test.com")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authCodeService.checkAuthCode(email, authCode))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("여러 이메일에 대한 인증코드 전송")
    void sendAuthCode_multipleEmails() {
        // given
        String[] emails = {"user1@test.com", "user2@test.com", "user3@test.com"};
        
        doNothing().when(authRedisService).checkDuration(anyString(), any(Duration.class));

        // when
        for (String email : emails) {
            authCodeService.sendAuthCode(email);
        }

        // then
        for (String email : emails) {
            then(authRedisService).should().checkDuration(
                    eq("AuthCode" + email), 
                    eq(Duration.ofMillis(300000L))
            );
            
            then(emailService).should().sendEmail(
                    eq(email), 
                    eq("당번 이메일 인증 번호"), 
                    anyString()
            );
            
            then(authRedisService).should().saveAuthCode(
                    eq("AuthCode" + email), 
                    anyString(), 
                    eq(Duration.ofMillis(300000L))
            );
        }
    }

    @Test
    @DisplayName("인증코드 키 형식 검증")
    void authCodeKey_format() {
        // given
        String email = "user@example.com";
        
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(authRedisService).checkDuration(anyString(), any(Duration.class));

        // when
        authCodeService.sendAuthCode(email);

        // then
        then(authRedisService).should().saveAuthCode(
                keyCaptor.capture(), 
                anyString(), 
                any(Duration.class)
        );
        
        String capturedKey = keyCaptor.getValue();
        assertThat(capturedKey).isEqualTo("AuthCodeuser@example.com");
        assertThat(capturedKey).startsWith("AuthCode");
        assertThat(capturedKey).contains(email);
    }

    @Test
    @DisplayName("인증코드 만료시간 설정 검증")
    void authCode_expirationTime() {
        // given
        String email = "test@test.com";
        Long customExpirationMillis = 600000L; // 10분
        ReflectionTestUtils.setField(authCodeService, "authCodeExpirationMillis", customExpirationMillis);
        
        ArgumentCaptor<Duration> durationCaptor = ArgumentCaptor.forClass(Duration.class);
        doNothing().when(authRedisService).checkDuration(anyString(), any(Duration.class));

        // when
        authCodeService.sendAuthCode(email);

        // then
        then(authRedisService).should().checkDuration(
                anyString(), 
                durationCaptor.capture()
        );
        
        Duration capturedDuration = durationCaptor.getValue();
        assertThat(capturedDuration).isEqualTo(Duration.ofMillis(600000L));
        assertThat(capturedDuration.toMinutes()).isEqualTo(10);
    }
}