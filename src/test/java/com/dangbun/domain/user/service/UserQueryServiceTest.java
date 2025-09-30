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
import com.dangbun.global.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class UserQueryServiceTest {

    @InjectMocks
    private UserQueryService userQueryService;

    @Mock
    private AuthCodeService authCodeService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthRedisService authRedisService;
    @Mock
    private JwtService jwtService;

    private User mockUser;
    private User disabledUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .password("encodedPassword")
                .enabled(true)
                .build();
        ReflectionTestUtils.setField(mockUser, "userId", 1L);

        disabledUser = User.builder()
                .name("비활성유저")
                .email("disabled@test.com")
                .password("encodedPassword")
                .enabled(false)
                .build();
        ReflectionTestUtils.setField(disabledUser, "userId", 2L);
    }

    @Test
    @DisplayName("비밀번호 찾기 인증코드 전송 - 성공")
    void sendFindPasswordAuthCode_success() {
        // given
        String email = "test@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));

        // when
        userQueryService.sendFindPasswordAuthCode(email);

        // then
        then(authCodeService).should().sendAuthCode(email);
    }

    @Test
    @DisplayName("비밀번호 찾기 인증코드 전송 - 존재하지 않는 이메일")
    void sendFindPasswordAuthCode_notFound() {
        // given
        String email = "notfound@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQueryService.sendFindPasswordAuthCode(email))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        // given
        PostUserLoginRequest request = new PostUserLoginRequest("test@test.com", "password123");
        
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtService.generateToken(mockUser)).willReturn(Map.of(
                "accessToken", "access-token-value",
                "refreshToken", "refresh-token-value"
        ));

        // when
        PostUserLoginResponse result = userQueryService.login(request);

        // then
        assertThat(result.accessToken()).isEqualTo("access-token-value");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-value");
        then(authRedisService).should().saveRefreshToken(1L, "refresh-token-value");
    }

    @Test
    @DisplayName("로그인 - 사용자 없음")
    void login_userNotFound() {
        // given
        PostUserLoginRequest request = new PostUserLoginRequest("notfound@test.com", "password123");
        
        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userQueryService.login(request))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    @DisplayName("로그인 - 비활성화된 사용자")
    void login_disabledUser() {
        // given
        PostUserLoginRequest request = new PostUserLoginRequest("disabled@test.com", "password123");
        
        given(userRepository.findByEmail("disabled@test.com")).willReturn(Optional.of(disabledUser));

        // when & then
        assertThatThrownBy(() -> userQueryService.login(request))
                .isInstanceOf(DeleteMemberException.class);
    }

    @Test
    @DisplayName("로그인 - 비밀번호 불일치")
    void login_wrongPassword() {
        // given
        PostUserLoginRequest request = new PostUserLoginRequest("test@test.com", "wrongPassword");
        
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        given(passwordEncoder.matches("wrongPassword", "encodedPassword")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userQueryService.login(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("로그아웃")
    void logout() {
        // given
        String bearerToken = "Bearer token-value";

        // when
        userQueryService.logout(bearerToken);

        // then
        then(authRedisService).should().deleteAndSetBlacklist(bearerToken);
    }

    @Test
    @DisplayName("내 정보 조회")
    void getMyInfo() {
        // given & when
        GetUserMyInfoResponse result = userQueryService.getMyInfo(mockUser);

        // then
        assertThat(result.name()).isEqualTo("테스트유저");
        assertThat(result.email()).isEqualTo("test@test.com");
    }
}