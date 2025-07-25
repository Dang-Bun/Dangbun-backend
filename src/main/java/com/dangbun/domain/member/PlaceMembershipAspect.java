package com.dangbun.domain.member;

import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.file.AccessDeniedException;

@Aspect
@Component
@RequiredArgsConstructor
public class PlaceMembershipAspect {
    private final MemberRepository memberRepository;


    @Before("@within(checkPlaceMembership) || @annotation(checkPlaceMembership)")
    public void validate(JoinPoint joinpoint, CheckPlaceMembership checkPlaceMembership) throws AccessDeniedException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!(principal instanceof CustomUserDetails userDetails)){
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        String placeIdParamName = checkPlaceMembership.placeIdParam();

        MethodSignature signature = (MethodSignature) joinpoint.getSignature();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinpoint.getArgs();

        Long placeId = null;
        for(int i=0; i<paramNames.length; i++){
            if (paramNames[i].equals(placeIdParamName)) {
                Object val = args[i];
                if (val instanceof Long) {
                    placeId = (Long) val;
                    break;
                }
            }
        }

        if (placeId == null) {
            throw new IllegalArgumentException("placeId 파라미터가 존재하지 않거나 Long 타입이 아닙니다.");
        }

        boolean exists = memberRepository.existsByUserIdAndPlaceId(userDetails.getUser().getId(), placeId);

        if (!exists) {
            throw new AccessDeniedException("해당 플레이스에 소속된 유저가 아닙니다.");
        }

    }
}
