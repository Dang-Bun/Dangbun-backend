package com.dangbun.global.aop;

import com.dangbun.global.aop.support.*;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.exception.custom.PlaceAccessDeniedException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.global.exception.RequiredParamMissingException;

import lombok.RequiredArgsConstructor;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import org.springframework.stereotype.Component;


import java.lang.reflect.Method;


import static com.dangbun.domain.member.response.status.MemberExceptionResponse.PLACE_ACCESS_DENIED;
import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Aspect
@Order(1)
@Component
@RequiredArgsConstructor
public class CheckPlaceMembershipAspect {
    private final MemberRepository memberRepository;

    @Around("@within(com.dangbun.global.aop.CheckPlaceMembership) || @annotation(com.dangbun.global.aop.CheckPlaceMembership)")
    public Object validate(ProceedingJoinPoint joinPoint) throws  Throwable {

        CustomUserDetails userDetails = SecuritySupport.currentUserOrThrow();

        CheckPlaceMembership checkPlaceMembership = AnnotationResolver.resolve(joinPoint, CheckPlaceMembership.class);

        String placeIdParamName = checkPlaceMembership.placeIdParam();
        Long placeId = RequestParamResolver.resolveLong(joinPoint, placeIdParamName);
        if (placeId == null) {
            throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
        }

        Member member = memberRepository.findWithPlaceByUserIdAndPlaceId(userDetails.getUser().getUserId(), placeId)
                .orElseThrow(()->new PlaceAccessDeniedException(PLACE_ACCESS_DENIED));

        try {
            MemberContext.set(member);
            return joinPoint.proceed();
        } finally {
            MemberContext.clear();
        }

    }


}
