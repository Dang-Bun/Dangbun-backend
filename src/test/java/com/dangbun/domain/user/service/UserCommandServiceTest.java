package com.dangbun.domain.user.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.user.dto.request.DeleteUserAccountRequest;
import com.dangbun.domain.user.dto.request.PostUserPasswordUpdateRequest;
import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.entity.LoginType;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserCommandServiceTest {

    @InjectMocks
    private UserCommandService userCommandService;

    @Mock
    private AuthCodeService authCodeService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private MemberRepository memberRepository;

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
    @DisplayName("회원가입 인증코드 전송 - 신규 이메일")
    void sendSignupAuthCode_newEmail() {
        // given
        String email = "new@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        userCommandService.sendSignupAuthCode(email);

        // then
        then(authCodeService).should().sendAuthCode(email);
        then(userRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("회원가입 인증코드 전송 - 비활성화된 사용자")
    void sendSignupAuthCode_disabledUser() {
        // given
        String email = "disabled@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.of(disabledUser));

        // when
        userCommandService.sendSignupAuthCode(email);

        // then
        then(userRepository).should().delete(disabledUser);
        then(authCodeService).should().sendAuthCode(email);
    }

    @Test
    @DisplayName("회원가입 인증코드 전송 - 이미 존재하는 활성 사용자")
    void sendSignupAuthCode_existingActiveUser() {
        // given
        String email = "test@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userCommandService.sendSignupAuthCode(email))
                .isInstanceOf(ExistEmailException.class);
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signup_success() {
        // given
        PostUserSignUpRequest request = new PostUserSignUpRequest(
                "new@test.com",
                "password123",
                "새사용자",
                "123456"
        );

        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword123");
        willDoNothing().given(authCodeService).checkAuthCode("new@test.com", "123456");

        // when
        userCommandService.signup(request);

        // then
        then(authCodeService).should().checkAuthCode("new@test.com", "123456");
        then(userRepository).should().save(argThat(user ->
                user.getName().equals("새사용자") &&
                        user.getEmail().equals("new@test.com") &&
                        user.getPassword().equals("encodedPassword123") &&
                        user.getLoginType().equals(LoginType.EMAIL) &&
                        user.getEnabled()
        ));
    }

    @Test
    @DisplayName("회원가입 - 이미 존재하는 이메일")
    void signup_existingEmail() {
        // given
        PostUserSignUpRequest request = new PostUserSignUpRequest(
                "test@test.com",
                "password123",
                "새사용자",
                "123456"
        );

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userCommandService.signup(request))
                .isInstanceOf(ExistEmailException.class);
    }

    @Test
    @DisplayName("회원가입 - 유효하지 않은 비밀번호")
    void signup_invalidPassword() {
        // given
        PostUserSignUpRequest request = new PostUserSignUpRequest(
                "new@test.com",
                "weak",  // 8자 미만
                "새사용자",
                "123456"
        );

        given(userRepository.findByEmail("new@test.com")).willReturn(Optional.empty());
        willDoNothing().given(authCodeService).checkAuthCode("new@test.com", "123456");

        // when & then
        assertThatThrownBy(() -> userCommandService.signup(request))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("비밀번호 업데이트 - 성공")
    void updatePassword_success() {
        // given
        PostUserPasswordUpdateRequest request = new PostUserPasswordUpdateRequest(
                "test@test.com",
                "654321",
                "newPassword123"
        );

        User testUser = User.builder()
                .name("테스트유저")
                .email("test@test.com")
                .password("oldPassword")
                .enabled(true)
                .build();

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(testUser));
        willDoNothing().given(authCodeService).checkAuthCode(anyString(), anyString());
        given(passwordEncoder.encode("newPassword123")).willReturn("encodedNewPassword");
        given(userRepository.save(any(User.class))).willReturn(testUser);

        // when
        userCommandService.updatePassword(request);

        // then
        then(authCodeService).should().checkAuthCode(eq("test@test.com"), eq("654321"));
        then(passwordEncoder).should().encode("newPassword123");
        then(userRepository).should().save(testUser);
        assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");
    }

    @Test
    @DisplayName("비밀번호 업데이트 - 사용자 없음")
    void updatePassword_userNotFound() {
        // given
        PostUserPasswordUpdateRequest request = new PostUserPasswordUpdateRequest(
                "notfound@test.com",
                "654321",
                "newPassword123"
        );

        given(userRepository.findByEmail("notfound@test.com")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userCommandService.updatePassword(request))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    @DisplayName("비밀번호 업데이트 - 유효하지 않은 비밀번호 패턴")
    void updatePassword_invalidPasswordPattern() {
        // given
        PostUserPasswordUpdateRequest request = new PostUserPasswordUpdateRequest(
                "test@test.com",
                "654321",
                "onlyletters"  // 숫자 없음
        );

        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(mockUser));
        willDoNothing().given(authCodeService).checkAuthCode("test@test.com", "654321");

        // when
        userCommandService.updatePassword(request);

        // then
        then(passwordEncoder).should(never()).encode(anyString());
        then(userRepository).should().save(mockUser);  // 변경되지 않은 상태로 저장
    }

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void deleteCurrentUser_success() {
        // given
        DeleteUserAccountRequest request = new DeleteUserAccountRequest("test@test.com");

        Member member1 = Member.builder().user(mockUser).build();
        Member member2 = Member.builder().user(mockUser).build();

        given(memberRepository.findALLByUser(mockUser)).willReturn(List.of(member1, member2));

        // when
        userCommandService.deleteCurrentUser(mockUser, request);

        // then
        then(userRepository).should().save(mockUser);
        then(memberRepository).should().deleteAll(List.of(member1, member2));
        assertThat(mockUser.getEnabled()).isFalse();
    }

    @Test
    @DisplayName("사용자 삭제 - 이메일 불일치")
    void deleteCurrentUser_emailMismatch() {
        // given
        DeleteUserAccountRequest request = new DeleteUserAccountRequest("wrong@test.com");

        // when & then
        assertThatThrownBy(() -> userCommandService.deleteCurrentUser(mockUser, request))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("사용자 삭제 - null 이메일")
    void deleteCurrentUser_nullEmail() {
        // given
        DeleteUserAccountRequest request = new DeleteUserAccountRequest(null);

        // when & then
        assertThatThrownBy(() -> userCommandService.deleteCurrentUser(mockUser, request))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    @DisplayName("유효한 비밀번호 패턴 테스트")
    void validPasswordPattern() {
        // given
        PostUserSignUpRequest validRequest1 = new PostUserSignUpRequest(
                "new1@test.com", "password1", "사용자1", "123456"
        );
        PostUserSignUpRequest validRequest2 = new PostUserSignUpRequest(
                "new2@test.com", "12345678a", "사용자2", "123456"
        );
        PostUserSignUpRequest validRequest3 = new PostUserSignUpRequest(
                "new3@test.com", "aB3456789012345678", "사용자3", "123456"  // 18자
        );

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encoded");
        willDoNothing().given(authCodeService).checkAuthCode(anyString(), anyString());

        // when & then
        assertThatCode(() -> {
            userCommandService.signup(validRequest1);
            userCommandService.signup(validRequest2);
            userCommandService.signup(validRequest3);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효하지 않은 비밀번호 패턴 테스트")
    void invalidPasswordPattern() {
        // given
        String[][] invalidPasswords = {
                {"short7", "너무 짧음 (8자 미만)"},
                {"verylongpasswordthatexceeds20chars123", "너무 긴 (20자 초과)"},
                {"onlyletters", "숫자 없음"},
                {"12345678", "문자 없음"},
                {"password!", "특수문자 포함"}
        };

        given(userRepository.findByEmail(anyString())).willReturn(Optional.empty());
        willDoNothing().given(authCodeService).checkAuthCode(anyString(), anyString());

        // when & then
        for (String[] invalidPassword : invalidPasswords) {
            PostUserSignUpRequest request = new PostUserSignUpRequest(
                    "test@test.com", invalidPassword[0], "사용자", "123456"
            );

            assertThatThrownBy(() -> userCommandService.signup(request))
                    .isInstanceOf(InvalidPasswordException.class)
                    .as("비밀번호 검증 실패: %s", invalidPassword[1]);
        }
    }
}