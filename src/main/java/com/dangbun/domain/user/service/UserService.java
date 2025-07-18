package com.dangbun.domain.user.service;

import com.dangbun.domain.user.dto.request.DeleteUserAccountRequest;
import com.dangbun.domain.user.dto.request.PostUserPasswordUpdateRequest;
import com.dangbun.domain.user.dto.request.PostUserSignUpRequest;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.*;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.RedisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Optional;
import java.util.Random;

import static com.dangbun.domain.user.exception.ErrorCode.*;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String CERT_CODE_PREFIX = "AuthCode";
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.auth-code-expiration-millis}")
    private Long authCodeExpirationMillis;

    public void createUser(){

    }


    public void login(){

    }

    public void logout(){

    }

    public void createPassword(){

    }






    public User findByEmail(String email){
        return userRepository.findByEmail(email).get();
    }

    public void sendAuthCode(String toEmail) {
        if(!isDuplicateEmail(toEmail)) {
            String title = "당번 이메일 인증 번호";
            String certCode = createAuthCode();
            emailService.sendEmail(toEmail, title, certCode);
            redisService.setValues(CERT_CODE_PREFIX + toEmail, certCode, Duration.ofMillis(this.authCodeExpirationMillis));
        }else{
            throw new RuntimeException("email already exist");
        }
    }

    private boolean isDuplicateEmail(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    private String createAuthCode(){
        int length= 6;
        try {
            Random random = SecureRandom.getInstanceStrong();
            StringBuilder builder = new StringBuilder();
            for(int i=0; i<length; i++){
                builder.append(random.nextInt(10));
            }
            return builder.toString();
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException();
        }
    }

    public void signup(@Valid PostUserSignUpRequest request) {

        String name = request.name();
        String rawPassword = request.password();
        String email = request.email();
        String certCode = request.certCode();


        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ExistEmailException(EXIST_EMAIL);
        }

        checkCertCode(email, certCode);

        if (!isValidPassword(rawPassword)) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        String encodePassword = passwordEncoder.encode(rawPassword);

        User user = User.builder().name(name)
                .password(encodePassword)
                .email(email)
                .build();

        userRepository.save(user);
    }

    private void checkCertCode(String email, String certCode) {
        if (!redisService.getValues(CERT_CODE_PREFIX+ email).equals(certCode)){
            throw new InvalidCertCodeException(INVALID_CERT_CODE);
        }
    }

    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }

    public void updatePassword(@Valid PostUserPasswordUpdateRequest request) {
        Optional<User> optional = userRepository.findByEmail(request.email());
        if(optional.isEmpty()){
            throw new UserNotFoundException(NO_SUCH_USER);
        }

        User user = optional.get();
        sendAuthCode(request.email());

        checkCertCode(request.email(), request.certCode());

        if(isValidPassword(request.password())){
            String encodedPassword = passwordEncoder.encode(request.password());
            user.updatePassword(encodedPassword);
        }

        userRepository.save(user);

    }

    public void deleteCurrentUser(CustomUserDetails customUser, DeleteUserAccountRequest request) {
        if(request.email() != null && customUser.getEmail().equals(request.email())){
            User user = customUser.getUser();
            user.deactivate();
            userRepository.save(user);
            return;
        }

        throw new InvalidEmailException(INVALID_EMAIL);
    }
}
