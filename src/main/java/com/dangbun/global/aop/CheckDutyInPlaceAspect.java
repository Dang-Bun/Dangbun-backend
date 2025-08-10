package com.dangbun.global.aop;


import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.exception.custom.DutyNotInPlaceFoundException;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.global.aop.support.AnnotationResolver;
import com.dangbun.global.aop.support.RequestParamResolver;
import com.dangbun.global.context.DutyContext;
import com.dangbun.global.exception.RequiredParamMissingException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import static com.dangbun.domain.duty.response.status.DutyExceptionResponse.DUTY_NOT_IN_PLACE;
import static com.dangbun.global.response.status.BaseExceptionResponse.REQUIRED_PARAM_MISSING;

@Aspect
@Component
@Order(2)
@RequiredArgsConstructor
public class CheckDutyInPlaceAspect {
    private final DutyRepository dutyRepository;
    private final AnnotationResolver annotationResolver;
    private final RequestParamResolver requestParamResolver;


    @Around("@annotation(com.dangbun.global.aop.CheckDutyInPlace)")
    public Object verify(ProceedingJoinPoint joinPoint)  throws Throwable {

        CheckDutyInPlace checkDutyInPlace = annotationResolver.resolve(joinPoint, CheckDutyInPlace.class);

        Long placeId = requestParamResolver.resolveLong(joinPoint, checkDutyInPlace.placeIdParam());
        if (placeId == null) {
            throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
        }
        Long dutyId  = requestParamResolver.resolveLong(joinPoint, checkDutyInPlace.dutyIdParam());
        if (dutyId == null) {
            throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
        }

        Duty duty = dutyRepository.findByDutyIdAndPlace_PlaceId(dutyId, placeId)
                .orElseThrow(() -> new DutyNotInPlaceFoundException(DUTY_NOT_IN_PLACE));

        try {
            DutyContext.set(duty);
            return joinPoint.proceed();
        } finally {
            DutyContext.clear();
        }
    }


}