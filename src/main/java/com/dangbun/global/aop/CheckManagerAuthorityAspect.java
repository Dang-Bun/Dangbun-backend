package com.dangbun.global.aop;

import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.exception.custom.MembershipUnauthorizedException;
import com.dangbun.global.context.MemberContext;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.dangbun.domain.member.exception.status.MemberExceptionResponse.INVALID_ROLE;
import static com.dangbun.domain.member.exception.status.MemberExceptionResponse.MEMBERSHIP_UNAUTHORIZED;


@Aspect
@Order(3)
@Component
public class CheckManagerAuthorityAspect {

    @Before("@within(com.dangbun.global.aop.CheckManagerAuthority) || @annotation(com.dangbun.global.aop.CheckManagerAuthority)")
    public void checkManagerAuthority() {
        MemberJpaEntity me = MemberContext.get();
        if (me == null) {
            throw new MembershipUnauthorizedException(MEMBERSHIP_UNAUTHORIZED);
        }

        MemberRole role = me.getRole();
        if (role != MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }
    }
}