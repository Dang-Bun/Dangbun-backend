package com.dangbun.domain.user.application.port.in.query;

import com.dangbun.domain.user.domain.AuthToken;

public record AuthTokenResult(
        String accessToken,
        String refreshToken
) {
    public static AuthTokenResult from(AuthToken token) {
        return new AuthTokenResult(token.accessToken(), token.refreshToken());
    }
}
