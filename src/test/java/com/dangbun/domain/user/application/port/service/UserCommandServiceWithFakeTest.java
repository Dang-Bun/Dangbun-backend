package com.dangbun.domain.user.application.port.service;

import com.dangbun.domain.member.adapter.out.persistence.FakeMemberRepository;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.domain.MemberRole;
import com.dangbun.domain.user.adapter.out.persistence.FakeAuthCodePort;
import com.dangbun.domain.user.adapter.out.persistence.FakeKakaoPort;
import com.dangbun.domain.user.adapter.out.persistence.FakePasswordEncoder;
import com.dangbun.domain.user.adapter.out.persistence.FakeUserRepository;
import com.dangbun.domain.user.application.port.in.command.DeleteUserCommand;
import com.dangbun.domain.user.application.port.in.command.SignupCommand;
import com.dangbun.domain.user.application.port.in.command.UpdatePasswordCommand;
import com.dangbun.domain.user.domain.LoginType;
import com.dangbun.domain.user.domain.User;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.exception.custom.InvalidCertCodeException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UserCommandService 테스트 - Fake 객체 사용
 * Mock 대신 인메모리 Fake 저장소를 사용하여 실제 동작을 검증
 */
class UserCommandServiceWithFakeTest {

    private UserCommandService userCommandService;

    private FakeUserRepository fakeUserRepository;
    private FakeAuthCodePort fakeAuthCodePort;
    private FakeKakaoPort fakeKakaoPort;
    private FakePasswordEncoder fakePasswordEncoder;
    private FakeMemberRepository fakeMemberRepository;

    @BeforeEach
    void setUp() {
        // Fake 저장소 초기화
        fakeUserRepository = new FakeUserRepository();
        fakeAuthCodePort = new FakeAuthCodePort();
        fakeKakaoPort = new FakeKakaoPort();
        fakePasswordEncoder = new FakePasswordEncoder();
        fakeMemberRepository = new FakeMemberRepository();

        // 서비스 생성
        userCommandService = new UserCommandService(
                fakeUserRepository,
                fakeUserRepository,
                fakeAuthCodePort,
                fakeKakaoPort,
                fakePasswordEncoder,
                fakeMemberRepository,
                fakeMemberRepository
        );
    }

    @AfterEach
    void tearDown() {
        fakeUserRepository.clear();
        fakeAuthCodePort.clear();
        fakeKakaoPort.clear();
        fakeMemberRepository.clear();
    }

    // ==================== sendSignupAuthCode 테스트 ====================

    @Test
    void 인증코드_발송_성공_신규이메일() {
        // given
        String email = "new@test.com";

        // when
        userCommandService.sendSignupAuthCode(email);

        // then
        assertThat(fakeAuthCodePort.wasSentTo(email)).isTrue();
    }

    @Test
    void 인증코드_발송_성공_비활성화된_기존사용자_삭제후_재발송() {
        // given
        String email = "disabled@test.com";
        User disabledUser = createUser(1L, "비활성 사용자", email, "encoded_password", LoginType.EMAIL, null, false);
        fakeUserRepository.save(disabledUser);

        // when
        userCommandService.sendSignupAuthCode(email);

        // then
        assertThat(fakeAuthCodePort.wasSentTo(email)).isTrue();
        assertThat(fakeUserRepository.findByEmail(email)).isEmpty();
    }

    @Test
    void 인증코드_발송_실패_이미_존재하는_활성사용자() {
        // given
        String email = "active@test.com";
        User activeUser = createUser(1L, "활성 사용자", email, "encoded_password", LoginType.EMAIL, null, true);
        fakeUserRepository.save(activeUser);

        // when, then
        assertThatThrownBy(() -> userCommandService.sendSignupAuthCode(email))
                .isInstanceOf(ExistEmailException.class);
    }

    // ==================== signup 테스트 ====================

    @Test
    void 회원가입_성공() {
        // given
        String email = "signup@test.com";
        String certCode = "123456";
        String password = "Password1";
        fakeAuthCodePort.setAuthCode(email, certCode);

        SignupCommand command = new SignupCommand("테스트유저", email, password, certCode);

        // when
        userCommandService.signup(command);

        // then
        Optional<User> savedUser = fakeUserRepository.findByEmail(email);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getName()).isEqualTo("테스트유저");
        assertThat(savedUser.get().getEmail()).isEqualTo(email);
        assertThat(savedUser.get().getPassword()).isEqualTo("encoded_" + password);
        assertThat(savedUser.get().getLoginType()).isEqualTo(LoginType.EMAIL);
        assertThat(savedUser.get().getEnabled()).isTrue();
    }

    @Test
    void 회원가입_실패_이미_존재하는_이메일() {
        // given
        String email = "existing@test.com";
        User existingUser = User.withoutId("기존유저", email, "encoded_password", LoginType.EMAIL, null, true);
        fakeUserRepository.save(existingUser);

        fakeAuthCodePort.setAuthCode(email, "123456");
        SignupCommand command = new SignupCommand("신규유저", email, "Password1", "123456");

        // when, then
        assertThatThrownBy(() -> userCommandService.signup(command))
                .isInstanceOf(ExistEmailException.class);
    }

    @Test
    void 회원가입_실패_잘못된_인증코드() {
        // given
        String email = "signup@test.com";
        fakeAuthCodePort.setAuthCode(email, "123456");
        SignupCommand command = new SignupCommand("테스트유저", email, "Password1", "wrongcode");

        // when, then
        assertThatThrownBy(() -> userCommandService.signup(command))
                .isInstanceOf(InvalidCertCodeException.class);
    }

    @Test
    void 회원가입_실패_유효하지_않은_비밀번호_숫자만() {
        // given
        String email = "signup@test.com";
        String certCode = "123456";
        fakeAuthCodePort.setAuthCode(email, certCode);
        SignupCommand command = new SignupCommand("테스트유저", email, "12345678", certCode);

        // when, then
        assertThatThrownBy(() -> userCommandService.signup(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void 회원가입_실패_유효하지_않은_비밀번호_문자만() {
        // given
        String email = "signup@test.com";
        String certCode = "123456";
        fakeAuthCodePort.setAuthCode(email, certCode);
        SignupCommand command = new SignupCommand("테스트유저", email, "abcdefgh", certCode);

        // when, then
        assertThatThrownBy(() -> userCommandService.signup(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void 회원가입_실패_유효하지_않은_비밀번호_8자_미만() {
        // given
        String email = "signup@test.com";
        String certCode = "123456";
        fakeAuthCodePort.setAuthCode(email, certCode);
        SignupCommand command = new SignupCommand("테스트유저", email, "Pass1", certCode);

        // when, then
        assertThatThrownBy(() -> userCommandService.signup(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    // ==================== updatePassword 테스트 ====================

    @Test
    void 비밀번호_변경_성공() {
        // given
        String email = "user@test.com";
        String certCode = "123456";
        String newPassword = "NewPassword1";

        User user = createUser(1L, "테스트유저", email, "encoded_OldPassword1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);
        fakeAuthCodePort.setAuthCode(email, certCode);

        UpdatePasswordCommand command = new UpdatePasswordCommand(email, certCode, newPassword);

        // when
        userCommandService.updatePassword(command);

        // then
        Optional<User> updatedUser = fakeUserRepository.findByEmail(email);
        assertThat(updatedUser).isPresent();
        assertThat(updatedUser.get().getPassword()).isEqualTo("encoded_" + newPassword);
    }

    @Test
    void 비밀번호_변경_실패_존재하지_않는_사용자() {
        // given
        String email = "nonexistent@test.com";
        fakeAuthCodePort.setAuthCode(email, "123456");
        UpdatePasswordCommand command = new UpdatePasswordCommand(email, "123456", "NewPassword1");

        // when, then
        assertThatThrownBy(() -> userCommandService.updatePassword(command))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    void 비밀번호_변경_실패_카카오_로그인_사용자() {
        // given
        String email = "kakao@test.com";
        String certCode = "123456";

        User kakaoUser = createUser(1L, "카카오유저", email, null, LoginType.KAKAO, "kakao123", true);
        fakeUserRepository.save(kakaoUser);
        fakeAuthCodePort.setAuthCode(email, certCode);

        UpdatePasswordCommand command = new UpdatePasswordCommand(email, certCode, "NewPassword1");

        // when, then
        assertThatThrownBy(() -> userCommandService.updatePassword(command))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    void 비밀번호_변경_실패_잘못된_인증코드() {
        // given
        String email = "user@test.com";
        User user = createUser(1L, "테스트유저", email, "encoded_OldPassword1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);
        fakeAuthCodePort.setAuthCode(email, "123456");

        UpdatePasswordCommand command = new UpdatePasswordCommand(email, "wrongcode", "NewPassword1");

        // when, then
        assertThatThrownBy(() -> userCommandService.updatePassword(command))
                .isInstanceOf(InvalidCertCodeException.class);
    }

    @Test
    void 비밀번호_변경_실패_유효하지_않은_비밀번호() {
        // given
        String email = "user@test.com";
        String certCode = "123456";

        User user = createUser(1L, "테스트유저", email, "encoded_OldPassword1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);
        fakeAuthCodePort.setAuthCode(email, certCode);

        UpdatePasswordCommand command = new UpdatePasswordCommand(email, certCode, "weak");

        // when, then
        assertThatThrownBy(() -> userCommandService.updatePassword(command))
                .isInstanceOf(InvalidPasswordException.class);
    }

    // ==================== deleteUser 테스트 ====================

    @Test
    void 회원탈퇴_성공_이메일_로그인_사용자() {
        // given
        Long userId = 1L;
        String email = "user@test.com";

        User user = createUser(userId, "테스트유저", email, "encoded_Password1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        DeleteUserCommand command = new DeleteUserCommand(userId, email);

        // when
        userCommandService.deleteUser(command);

        // then
        Optional<User> deletedUser = fakeUserRepository.findById(userId);
        assertThat(deletedUser).isPresent();
        assertThat(deletedUser.get().getEnabled()).isFalse();
    }

    @Test
    void 회원탈퇴_성공_카카오_로그인_사용자_연동해제() {
        // given
        Long userId = 1L;
        String email = "kakao@test.com";
        String socialId = "kakao123";

        User kakaoUser = createUser(userId, "카카오유저", email, null, LoginType.KAKAO, socialId, true);
        fakeUserRepository.save(kakaoUser);

        DeleteUserCommand command = new DeleteUserCommand(userId, email);

        // when
        userCommandService.deleteUser(command);

        // then
        Optional<User> deletedUser = fakeUserRepository.findById(userId);
        assertThat(deletedUser).isPresent();
        assertThat(deletedUser.get().getEnabled()).isFalse();
        assertThat(fakeKakaoPort.wasUnlinked(socialId)).isTrue();
    }

    @Test
    void 회원탈퇴_성공_연관된_멤버_삭제() {
        // given
        Long userId = 1L;
        String email = "user@test.com";

        User user = createUser(userId, "테스트유저", email, "encoded_Password1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        Member member1 = Member.withId(
                1L,
                MemberRole.MEMBER,
                "멤버1",
                true,
                Map.of(),
                10L,
                userId,
                LocalDateTime.now()
        );
        Member member2 = Member.withId(
                2L,
                MemberRole.MANAGER,
                "멤버2",
                true,
                Map.of(),
                20L,
                userId,
                LocalDateTime.now()
        );
        fakeMemberRepository.save(member1);
        fakeMemberRepository.save(member2);

        DeleteUserCommand command = new DeleteUserCommand(userId, email);

        // when
        userCommandService.deleteUser(command);

        // then
        assertThat(fakeMemberRepository.findByUserId(userId)).isEmpty();
    }

    @Test
    void 회원탈퇴_실패_존재하지_않는_사용자() {
        // given
        Long userId = 999L;
        String email = "nonexistent@test.com";
        DeleteUserCommand command = new DeleteUserCommand(userId, email);

        // when, then
        assertThatThrownBy(() -> userCommandService.deleteUser(command))
                .isInstanceOf(NoSuchUserException.class);
    }

    @Test
    void 회원탈퇴_실패_이메일_불일치() {
        // given
        Long userId = 1L;
        String email = "user@test.com";

        User user = createUser(userId, "테스트유저", email, "encoded_Password1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        DeleteUserCommand command = new DeleteUserCommand(userId, "wrong@test.com");

        // when, then
        assertThatThrownBy(() -> userCommandService.deleteUser(command))
                .isInstanceOf(InvalidEmailException.class);
    }

    @Test
    void 회원탈퇴_실패_이메일_null() {
        // given
        Long userId = 1L;
        String email = "user@test.com";

        User user = createUser(userId, "테스트유저", email, "encoded_Password1", LoginType.EMAIL, null, true);
        fakeUserRepository.save(user);

        DeleteUserCommand command = new DeleteUserCommand(userId, null);

        // when, then
        assertThatThrownBy(() -> userCommandService.deleteUser(command))
                .isInstanceOf(InvalidEmailException.class);
    }

    // ==================== 헬퍼 메서드 ====================

    private User createUser(Long userId, String name, String email, String password, LoginType loginType, String socialId, Boolean enabled) {
        return User.withId(
                new User.UserId(userId),
                name,
                email,
                password,
                loginType,
                socialId,
                enabled,
                LocalDateTime.now()
        );
    }
}
