package com.dangbun.domain.user.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.user.client.KakaoApiClient;
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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class UserCommandService {

    private final AuthCodeService authCodeService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;

    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Transactional
    public void sendSignupAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isEmpty()) {
            authCodeService.sendAuthCode(toEmail);
            return;
        }

        User user = getUserByEmail(toEmail).get();
        if (!user.getEnabled()) {
            userRepository.delete(user);
            authCodeService.sendAuthCode(toEmail);
            return;
        }
        throw new ExistEmailException(EXIST_EMAIL);
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

        authCodeService.checkAuthCode(email, certCode);

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

    @Transactional
    public void updatePassword(@Valid PostUserPasswordUpdateRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        authCodeService.checkAuthCode(request.email(), request.certCode());

        if (isValidPassword(request.password())) {
            String encodedPassword = passwordEncoder.encode(request.password());
            user.updatePassword(encodedPassword);
        }

        userRepository.save(user);

    }

    @Transactional
    public void deleteCurrentUser(User user, DeleteUserAccountRequest request) {
        validateDeleteRequest(user, request);
        if (user.getLoginType().equals(LoginType.KAKAO)) {
            unlinkKakaoAccount(user.getSocialId());
        }

        /**
         * soft delete
         */
//        user.deactivate();
//        userRepository.save(user);

        /**
         * hard delete
         */
        userRepository.delete(user);

        List<Member> members = memberRepository.findALLByUser(user);
        memberRepository.deleteAll(members);

        return;
    }

    private void unlinkKakaoAccount(String socialId) {
        try {
            kakaoApiClient.unlink("KakaoAK " + adminKey, "user_id", Long.parseLong(socialId));
        } catch (feign.FeignException e) {
            if (e.contentUTF8().contains("-101")) {
                return;
            }
            throw e;
        }
    }

    private void validateDeleteRequest(User user, DeleteUserAccountRequest request) {
        if (request.email() == null || !user.getEmail().equals(request.email())) {
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }

}
