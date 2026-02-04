package com.dangbun.global.aop;

import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.member.original.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.original.exception.custom.MembershipUnauthorizedException;
import com.dangbun.global.context.MemberContext;

import static com.dangbun.domain.member.original.response.status.MemberExceptionResponse.*;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


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