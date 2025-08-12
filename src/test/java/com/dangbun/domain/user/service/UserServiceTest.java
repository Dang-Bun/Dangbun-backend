package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;



import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.INVALID_EMAIL;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    RedisService redisService;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("repository에 저장되지 않은 이메일은 오류 발생")
    void sendFindPasswordAuthCodeTest() {
        // given
        String toEmail = "test@test.com";
        when(userRepository.findByEmail(toEmail)).thenReturn(Optional.empty());

        // when & then
        assertThrows(InvalidEmailException.class,()-> userService.sendFindPasswordAuthCode(toEmail));
    }

    @Test
    @DisplayName("")
    void sendSignupAuthCodeTest() {
        // given
        String toEmail = "test@test.com";

        // when

        // then
    }

    @Test
    @DisplayName("회원가입 테스트")
    void signup() {
        // given
        Long memberId = 1L;
        String email = "test@test.com";
        String name = "이름";
        String password = "1234abcd";
        String certCode = "123abc";

        String CERT_CODE_PREFIX = "AuthCode";


        PostUserSignUpRequest request = new PostUserSignUpRequest(email, password, name, certCode);
        User testUser = User.builder()
                .name(name)
                .password(password)
                .email(email)
                .enabled(true)
                .build();
        when(redisService.getValues(CERT_CODE_PREFIX + email)).thenReturn(certCode);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // when
        User user = userService.signup(request);

        // then
        assertThat(user).isNotNull();
        assertThat(user.getName()).isEqualTo(name);
    }
}