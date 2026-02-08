package com.dangbun.domain.user.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.user.application.port.in.command.AuthUseCase;
import com.dangbun.domain.user.application.port.in.command.EmailLoginCommand;
import com.dangbun.domain.user.application.port.in.command.KakaoLoginCommand;
import com.dangbun.domain.user.application.port.out.*;
import com.dangbun.domain.user.application.port.in.command.*;
import com.dangbun.domain.user.application.port.in.query.AuthTokenResult;
import com.dangbun.domain.user.application.port.out.*;
import com.dangbun.domain.user.domain.AuthToken;
import com.dangbun.domain.user.domain.KakaoUserInfo;
import com.dangbun.domain.user.domain.LoginType;
import com.dangbun.domain.user.domain.User;
import com.dangbun.domain.user.exception.custom.DeleteMemberException;
import com.dangbun.domain.user.exception.custom.InvalidPasswordException;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.*;

@UseCase
@RequiredArgsConstructor
@Transactional
public class AuthService implements AuthUseCase {

    private final UserQueryPort userQueryPort;
    private final UserCommandPort userCommandPort;
    private final TokenPort tokenPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final KakaoOAuthPort kakaoOAuthPort;

    @Override
    public AuthTokenResult loginWithEmail(EmailLoginCommand command) {
        User user = userQueryPort.findByEmail(command.getEmail())
                .orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER));

        if (!user.getEnabled()) {
            throw new DeleteMemberException(DELETE_MEMBER);
        }

        if (!user.isEmailLogin()) {
            throw new NoSuchUserException(NO_SUCH_USER);
        }

        if (!passwordEncoderPort.matches(command.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException(INVALID_PASSWORD);
        }

        AuthToken token = tokenPort.generateTokens(user.getUserId().value(), user.getEmail());
        tokenPort.saveRefreshToken(user.getUserId().value(), token.refreshToken());

        return AuthTokenResult.from(token);
    }

    @Override
    public AuthTokenResult loginWithKakao(KakaoLoginCommand command) {
        // 카카오에서 Access Token 발급
        String kakaoAccessToken = kakaoOAuthPort.fetchAccessToken(command.getCode());

        // 카카오에서 사용자 정보 조회
        KakaoUserInfo userInfo = kakaoOAuthPort.fetchUserInfo(kakaoAccessToken);

        // 기존 사용자 조회 또는 자동 회원가입
        User user = userQueryPort.findBySocialId(userInfo.socialId())
                .orElseGet(() -> registerKakaoUser(userInfo));

        // JWT 토큰 생성
        AuthToken token = tokenPort.generateTokens(user.getUserId().value(), user.getEmail());
        tokenPort.saveRefreshToken(user.getUserId().value(), token.refreshToken());

        return AuthTokenResult.from(token);
    }

    private User registerKakaoUser(KakaoUserInfo userInfo) {
        User newUser = User.withoutIdBuilder()
                .name(userInfo.nickname())
                .email(userInfo.email())
                .loginType(LoginType.KAKAO)
                .socialId(userInfo.socialId())
                .enabled(true)
                .build();

        return userCommandPort.save(newUser);
    }
}
