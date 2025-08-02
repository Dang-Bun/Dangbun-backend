package com.dangbun.domain.checklist;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.exception.ChecklistAccessDeniedException;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.member.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.user.entity.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.file.AccessDeniedException;

@Aspect
@Component
@RequiredArgsConstructor

public class ChecklistMembershipAspect {

    private final MemberCleaningRepository memberCleaningRepository;
    private final ChecklistRepository checklistRepository;

    @Before("@within(com.dangbun.domain.checklist.CheckChecklistMembership) || @annotation(com.dangbun.domain.checklist.CheckChecklistMembership)")
    public void validate(JoinPoint joinPoint) throws AccessDeniedException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof CustomUserDetails)) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        CheckChecklistMembership checkChecklistMembership = method.getAnnotation(CheckChecklistMembership.class);

        if (checkChecklistMembership == null) {
            checkChecklistMembership = joinPoint.getTarget().getClass().getAnnotation(CheckChecklistMembership.class);
        }

        if (checkChecklistMembership == null) {
            throw new IllegalStateException("checkChecklistMembership 애너테이션을 찾을 수 없습니다.");
        }

        String checklistIdParamName = checkChecklistMembership.checklistIdParam();
        String[] paramNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        Long checklistId = null;
        for (int i = 0; i < paramNames.length; i++) {
            if (paramNames[i].equals(checklistIdParamName) && args[i] instanceof Long) {
                checklistId = (Long) args[i];
                break;
            }
        }

        if (checklistId == null) {
            throw new IllegalArgumentException("checklistId 파라미터가 존재하지 않거나 Long 타입이 아닙니다.");
        }

        Member me = MemberContext.get();

        Cleaning cleaning = memberCleaningRepository.findCleaningByMemberId(me.getMemberId());
        Checklist checklist = checklistRepository
                .findByChecklistIdAndCleaning_CleaningId(checklistId, cleaning.getCleaningId())
                .orElseThrow(()->new ChecklistAccessDeniedException(ChecklistExceptionResponse.CHECKLIST_ACCESS_DENIED));

        ChecklistContext.set(checklist);

    }

    @After("@within(com.dangbun.domain.checklist.CheckChecklistMembership) || @annotation(com.dangbun.domain.checklist.CheckChecklistMembership)")
    public void clearChecklistContext(){
        ChecklistContext.clear();
    }

}
