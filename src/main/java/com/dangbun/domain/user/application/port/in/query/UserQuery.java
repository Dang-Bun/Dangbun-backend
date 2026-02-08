package com.dangbun.domain.user.application.port.in.query;

public interface UserQuery {

    void sendFindPasswordAuthCode(String email);

    void logout(Long userId, String bearerToken);

    UserInfoResult getMyInfo(Long userId);
}
