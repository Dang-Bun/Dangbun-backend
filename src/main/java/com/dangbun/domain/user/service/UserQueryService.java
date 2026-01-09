package com.dangbun.domain.user.service;

import com.dangbun.domain.user.client.KakaoApiClient;
import com.dangbun.domain.user.dto.response.GetUserMyInfoResponse;
import com.dangbun.domain.user.entity.LoginType;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.redis.AuthRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@RequiredArgsConstructor
@Service
public class UserQueryService {

    private final AuthCodeService authCodeService;
    private final UserRepository userRepository;
    private final AuthRedisService authRedisService;
    private final KakaoApiClient kakaoApiClient;

    @Value("${kakao.admin-key}")
    private String adminKey;

    @Transactional(readOnly = true)
    public void sendFindPasswordAuthCode(String toEmail) {
        if (getUserByEmail(toEmail).isPresent()) {
            authCodeService.sendAuthCode(toEmail);
        } else{
            throw new InvalidEmailException(INVALID_EMAIL);
        }
    }

    public void logout(User user, String bearerToken) {
        /**
         * 개인정보 보호를 위함(필수는 아님)
         */
        if(user.getLoginType().equals(LoginType.KAKAO)){
            kakaoApiClient.logout("KakaoAK " + adminKey, "user_id", Long.parseLong(user.getSocialId()));
        }
        authRedisService.deleteAndSetBlacklist(bearerToken);
    }

    public GetUserMyInfoResponse getMyInfo(User user) {
        return GetUserMyInfoResponse.from(user);
    }

    private Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

}
