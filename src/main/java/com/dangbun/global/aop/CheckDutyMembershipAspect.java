package com.dangbun.global.aop;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.exception.custom.DutyAccessDeniedException;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.global.aop.support.AnnotationResolver;
import com.dangbun.global.aop.support.RequestParamResolver;
import com.dangbun.global.aop.support.SecuritySupport;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.exception.RequiredParamMissingException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;


import static com.dangbun.domain.member.response.status.MemberExceptionResponse.DUTY_ACCESS_DENIED;
import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckDutyMembershipAspect {
    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;

    @Around("@within(com.dangbun.global.aop.CheckDutyMembership) || @annotation(com.dangbun.global.aop.CheckDutyMembership)")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {

        CustomUserDetails userDetails = SecuritySupport.currentUserOrThrow();

        CheckDutyMembership checkDutyMembership = AnnotationResolver.resolve(joinPoint, CheckDutyMembership.class);

        String dutyIdParamName = checkDutyMembership.dutyIdParam();
        Long dutyId = RequestParamResolver.resolveLong(joinPoint, dutyIdParamName);
        if (dutyId == null) {
            throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
        }

        MemberDuty memberDuty = memberDutyRepository
                .findMemberDutyWithDutyAndPlace(dutyId, userDetails.getUser().getUserId())
                .orElseThrow(() -> new DutyAccessDeniedException(DUTY_ACCESS_DENIED));

        Member member = memberDuty.getMember();
        Duty duty = memberDuty.getDuty();

        String placeIdParam = checkDutyMembership.placeIdParam();
        if (placeIdParam != null && !placeIdParam.isBlank()) {
            Long placeId = RequestParamResolver.resolveLong(joinPoint, placeIdParam);
            if (placeId == null){
                throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
            }

            if (!duty.getPlace().getPlaceId().equals(placeId)) {
                throw new DutyAccessDeniedException(DUTY_ACCESS_DENIED);
            }
        }

        try {
            MemberContext.set(member);
            DutyContext.set(duty);
            return joinPoint.proceed();
        } finally {
            MemberContext.clear();
            DutyContext.clear();
        }
    }

}
