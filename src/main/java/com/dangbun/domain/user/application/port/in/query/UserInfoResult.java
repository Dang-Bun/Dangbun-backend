package com.dangbun.domain.user.application.port.in.query;

import com.dangbun.domain.user.domain.User;

public record UserInfoResult(
        String name,
        String email
) {
    public static UserInfoResult from(User user) {
        return new UserInfoResult(user.getName(), user.getEmail());
    }
}
