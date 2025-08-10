package com.dangbun.global.aop.support;

import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.global.exception.UnautheniticatedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static com.dangbun.global.response.status.BaseExceptionResponse.*;

public final class SecuritySupport {

    private SecuritySupport() {}

    public static CustomUserDetails currentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
            throw new UnautheniticatedException(AUTH_UNAUTHENTICATED);
        }
        if (!(auth.getPrincipal() instanceof CustomUserDetails details)) {
            throw new UnautheniticatedException(AUTH_UNAUTHENTICATED);
        }
        return details;
    }
}
