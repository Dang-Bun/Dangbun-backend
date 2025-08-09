package com.dangbun.global.aop;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.*;
import com.dangbun.global.context.MemberContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.*;


@Slf4j
@Aspect
@Component
public class CheckManagerAuthorityAspect {

    @Before("@within(com.dangbun.global.aop.CheckManagerAuthority) || @annotation(com.dangbun.global.aop.CheckManagerAuthority)")
    public void checkManagerAuthority() {
        Member me = MemberContext.get();
        if (me == null) {
            throw new PlaceMembershipUnauthorizedException(PLACE_MEMBERSHIP_UNAUTHORIZED);
        }

        MemberRole role = me.getRole();
        if (role != MemberRole.MANAGER) {
            throw new InvalidRoleException(INVALID_ROLE);
        }
    }
}