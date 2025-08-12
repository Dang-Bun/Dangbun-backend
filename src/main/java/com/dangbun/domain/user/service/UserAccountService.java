package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.DeleteUserAccountRequest;
import com.dangbun.domain.user.dto.request.PostUserPasswordUpdateRequest;
import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.*;
import com.dangbun.domain.user.policy.PasswordPolicy;
import com.dangbun.domain.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@Service
@RequiredArgsConstructor
public class UserAccountService {


    private final PasswordPolicy passwordPolicy;
    private final UserRepository userRepository;
    private final AuthCodeService authCodeService;
    private final PasswordEncoder passwordEncoder;


    @Transactional
    public User signup(@Valid PostUserSignUpRequest request) {

        String name = request.name();
        String rawPassword = request.password();
        String email = request.email();
        String certCode = request.certCode();


        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ExistEmailException(EXIST_EMAIL);
        }

        authCodeService.checkAuthCode(email, certCode);

        if (passwordPolicy.isValidPassword(rawPassword)) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        String encodePassword = passwordEncoder.encode(rawPassword);

        User user = User.builder().
                name(name)
                .enabled(true)
                .password(encodePassword)
                .email(email)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(@Valid PostUserPasswordUpdateRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        authCodeService.checkAuthCode(request.email(), request.certCode());

        if (passwordPolicy.isValidPassword(request.password())) {
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


    public GetUserMyInfoResponse getMyInfo(User user) {
        return GetUserMyInfoResponse.from(user);
    }



}
