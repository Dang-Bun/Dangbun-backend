package com.dangbun.domain.user.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.member.application.port.out.MemberCommandPort;
import com.dangbun.domain.member.application.port.out.MemberQueryPort;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.user.application.port.in.command.DeleteUserCommand;
import com.dangbun.domain.user.application.port.in.command.SignupCommand;
import com.dangbun.domain.user.application.port.in.command.UpdatePasswordCommand;
import com.dangbun.domain.user.application.port.in.command.UserCommandUseCase;
import com.dangbun.domain.user.application.port.out.AuthCodePort;
import com.dangbun.domain.user.application.port.out.KakaoPort;
import com.dangbun.domain.user.application.port.out.UserCommandPort;
import com.dangbun.domain.user.application.port.out.UserQueryPort;
import com.dangbun.domain.user.exception.custom.ExistEmailException;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.application.port.in.command.*;
import com.dangbun.domain.user.application.port.out.*;
import com.dangbun.domain.user.domain.LoginType;
import com.dangbun.domain.user.domain.User;
import com.dangbun.domain.user.exception.custom.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;


@UseCase
@RequiredArgsConstructor
@Transactional
public class UserCommandService implements UserCommandUseCase {

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;
    private final AuthCodePort authCodePort;
    private final KakaoPort kakaoPort;
    private final PasswordEncoder passwordEncoder;

    /*
     * TODO: Member 도메인 완전 분리 시 전용 인커밍 포트로 교체
     */
    private final MemberQueryPort memberQueryPort;
    private final MemberCommandPort memberCommandPort;

    @Override
    public void sendSignupAuthCode(String email) {
        userQueryPort.findByEmail(email).ifPresentOrElse(
                user -> {
                    if (!user.getEnabled()) {
                        userCommandPort.delete(user);
                        authCodePort.sendAuthCode(email);
                    } else {
                        throw new ExistEmailException(EXIST_EMAIL);
                    }
                },
                () -> authCodePort.sendAuthCode(email)
        );
    }

    @Override
    public void signup(SignupCommand command) {
        if (userQueryPort.existsByEmail(command.getEmail())) {
            throw new ExistEmailException(EXIST_EMAIL);
        }

        authCodePort.checkAuthCode(command.getEmail(), command.getCertCode());

        if (!isValidPassword(command.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(command.getPassword());

        User user = User.withoutIdBuilder()
                .name(command.getName())
                .email(command.getEmail())
                .password(encodedPassword)
                .loginType(LoginType.EMAIL)
                .enabled(true)
                .build();

        userCommandPort.save(user);
    }

    @Override
    public void updatePassword(UpdatePasswordCommand command) {
        User user = userQueryPort.findByEmail(command.getEmail())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (!user.isEmailLogin()) {
            throw new NoSuchUserException(NO_SUCH_USER);
        }

        authCodePort.checkAuthCode(command.getEmail(), command.getCertCode());

        if (!isValidPassword(command.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        String encodedPassword = passwordEncoder.encode(command.getPassword());
        user.updatePassword(encodedPassword);
        userCommandPort.save(user);
    }

    @Override
    public void deleteUser(DeleteUserCommand command) {
        User user = userQueryPort.findById(command.getUserId())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        validateDeleteRequest(user, command.getEmail());

        if (user.isKakaoLogin()) {
            kakaoPort.unlinkAccount(user.getSocialId());
        }

        // Soft delete
        user.deactivate();
        userCommandPort.save(user);

        // 연관된 멤버 삭제
        List<Member> members = memberQueryPort.findByUserId(user.getUserId().value());
        for (Member member : members) {
            memberCommandPort.delete(member);
        }
    }

    private void validateDeleteRequest(User user, String requestEmail) {
        if (requestEmail == null || !user.getEmail().equals(requestEmail)) {
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }
}
