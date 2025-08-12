package com.dangbun.domain.user.policy;

public class PasswordPolicy {
    private static final String PASSWORD_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,20}$";

    private boolean isValidPassword(String password) {
        return password != null && password.matches(PASSWORD_PATTERN);
    }
}
