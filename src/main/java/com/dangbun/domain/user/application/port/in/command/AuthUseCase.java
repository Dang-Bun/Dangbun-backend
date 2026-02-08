package com.dangbun.domain.user.application.port.in.command;

import com.dangbun.domain.user.application.port.in.query.AuthTokenResult;

/**
 * 인증 관련 Use Case
 */
public interface AuthUseCase {

    AuthTokenResult loginWithEmail(EmailLoginCommand command);

    AuthTokenResult loginWithKakao(KakaoLoginCommand command);
}
