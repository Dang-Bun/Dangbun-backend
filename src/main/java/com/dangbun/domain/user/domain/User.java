package com.dangbun.domain.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * User 도메인 모델
 * JPA 엔티티와 분리된 순수한 도메인 객체
 */
@AllArgsConstructor
public class User {

    @Getter
    private final UserId userId;

    @Getter
    private String name;

    @Getter
    private final String email;

    @Getter
    private String password;

    @Getter
    private final LoginType loginType;

    @Getter
    private final String socialId;

    @Getter
    private Boolean enabled;

    @Getter
    private final LocalDateTime createdAt;

    @Builder(builderMethodName = "withoutIdBuilder")
    public static User withoutId(String name, String email, String password, LoginType loginType, String socialId, Boolean enabled) {
        return new User(
                null,
                name,
                email,
                loginType == LoginType.EMAIL ? password : null,
                loginType == null ? LoginType.EMAIL : loginType,
                socialId,
                enabled,
                null
        );
    }

    @Builder(builderMethodName = "withIdBuilder")
    public static User withId(UserId userId, String name, String email, String password, LoginType loginType, String socialId, Boolean enabled, LocalDateTime createdAt) {
        return new User(userId, name, email, password, loginType, socialId, enabled, createdAt);
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.enabled = false;
    }

    public void activate() {
        this.enabled = true;
    }

    public boolean isEmailLogin() {
        return this.loginType == LoginType.EMAIL;
    }

    public boolean isKakaoLogin() {
        return this.loginType == LoginType.KAKAO;
    }

    public record UserId(Long value) {
    }
}
