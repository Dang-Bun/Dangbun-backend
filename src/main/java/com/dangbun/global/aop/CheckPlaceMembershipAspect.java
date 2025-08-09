package com.dangbun.global.aop;

import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.exception.custom.PlaceAccessDeniedException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import com.dangbun.global.exception.RequiredParamMissingException;
import com.dangbun.global.exception.UnautheniticatedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.Method;
import java.util.Map;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.PLACE_ACCESS_DENIED;
import static com.dangbun.global.response.status.BaseExceptionResponse.*;

@Aspect
@Component
@RequiredArgsConstructor
public class CheckPlaceMembershipAspect {
    private final MemberRepository memberRepository;


    @Around("@within(com.dangbun.global.aop.CheckPlaceMembership) || @annotation(com.dangbun.global.aop.CheckPlaceMembership)")
    public Object validate(ProceedingJoinPoint joinPoint) throws  Throwable {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth == null || auth.getPrincipal() == null || "anonymousUser".equals(auth.getPrincipal())) {
                throw new UnautheniticatedException(AUTH_UNAUTHENTICATED);
            }

            if (!(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
                throw new UnautheniticatedException(AUTH_UNAUTHENTICATED);
            }

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            CheckPlaceMembership checkPlaceMembership = method.getAnnotation(CheckPlaceMembership.class);

            if (checkPlaceMembership == null) {
                checkPlaceMembership = joinPoint.getTarget().getClass().getAnnotation(CheckPlaceMembership.class);
            }

            String placeIdParamName = checkPlaceMembership.placeIdParam();
            Long placeId = resolvePlaceId(joinPoint, placeIdParamName);
            if (placeId == null) {
                throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
            }

            Member member = memberRepository.findWithPlaceByUserIdAndPlaceId(userDetails.getUser().getUserId(), placeId)
                    .orElseThrow(()->new PlaceAccessDeniedException(PLACE_ACCESS_DENIED));

            MemberContext.set(member);

            return joinPoint.proceed();
        } finally {
            MemberContext.clear();
        }
    }

    private Long resolvePlaceId(JoinPoint joinPoint, String placeIdParamName) {
        // URI PathVariable에서 시도
        RequestAttributes ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            HttpServletRequest req = sra.getRequest();

            Object vars = req.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            if (vars instanceof Map<?, ?> map) {
                Object v = map.get(placeIdParamName);
                if (v != null) {
                    try { return Long.valueOf(v.toString()); } catch (NumberFormatException ignored) {}
                }
            }

            // QueryParam에서 시도
            String qp = req.getParameter(placeIdParamName);
            if (qp != null) {
                try { return Long.valueOf(qp); } catch (NumberFormatException ignored) {}
            }
        }

        // 메서드 파라미터에서 찾기
        MethodSignature sig = (MethodSignature) joinPoint.getSignature();
        String[] names = sig.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (names != null) {
            for (int i = 0; i < names.length; i++) {
                if (placeIdParamName.equals(names[i]) && args[i] != null) {
                    Object a = args[i];
                    if (a instanceof Long l) return l;
                    if (a instanceof Number n) return n.longValue();
                    try { return Long.valueOf(a.toString()); } catch (NumberFormatException ignored) {}
                }
            }
        }

        return null;
    }

}
