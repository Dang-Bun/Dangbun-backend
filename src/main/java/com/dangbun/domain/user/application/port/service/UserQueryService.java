package com.dangbun.domain.user.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.user.application.port.in.query.UserInfoResult;
import com.dangbun.domain.user.application.port.in.query.UserQuery;
import com.dangbun.domain.user.application.port.out.AuthCodePort;
import com.dangbun.domain.user.application.port.out.KakaoPort;
import com.dangbun.domain.user.application.port.out.TokenBlacklistPort;
import com.dangbun.domain.user.application.port.out.UserQueryPort;
import com.dangbun.domain.user.application.port.out.*;
import com.dangbun.domain.user.domain.User;
import com.dangbun.domain.user.exception.custom.InvalidEmailException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;


@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryService implements UserQuery {

    private final UserQueryPort userQueryPort;
    private final AuthCodePort authCodePort;
    private final KakaoPort kakaoPort;
    private final TokenBlacklistPort tokenBlacklistPort;

    @Override
    public void sendFindPasswordAuthCode(String email) {
        userQueryPort.findByEmail(email)
                .orElseThrow(() -> new InvalidEmailException(INVALID_EMAIL));

        authCodePort.sendAuthCode(email);
    }

    @Override
    @Transactional
    public void logout(Long userId, String bearerToken) {
        User user = userQueryPort.findById(userId)
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (user.isKakaoLogin()) {
            kakaoPort.logout(user.getSocialId());
        }

        tokenBlacklistPort.blacklistToken(bearerToken);
    }

    @Override
    public UserInfoResult getMyInfo(Long userId) {
        User user = userQueryPort.findById(userId)
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        return UserInfoResult.from(user);
    }
}
