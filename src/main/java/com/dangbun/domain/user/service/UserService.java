package com.dangbun.domain.user.service;

import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.email.EmailService;
import com.dangbun.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private static final String AUTH_CODE_PREFIX = "AuthCode";

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final RedisService redisService;

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

    public void deleteUser(){

    }




    public User findByEmail(String email){
        return userRepository.findByEmail(email).get();
    }

    public void sendAuthCode(String toEmail) {
        if(!isDuplicateEmail(toEmail)) {
            String title = "당번 이메일 인증 번호";
            String authCode = createAuthCode();
            emailService.sendEmail(toEmail, title, authCode);
            redisService.setValues(AUTH_CODE_PREFIX + toEmail, authCode, Duration.ofMillis(this.authCodeExpirationMillis));
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
}
