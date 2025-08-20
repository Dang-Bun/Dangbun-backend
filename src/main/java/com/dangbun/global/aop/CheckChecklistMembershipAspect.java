package com.dangbun.global.aop;

import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.global.aop.support.AnnotationResolver;
import com.dangbun.global.aop.support.RequestParamResolver;
import com.dangbun.global.aop.support.SecuritySupport;
import com.dangbun.global.context.ChecklistContext;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.exception.custom.ChecklistAccessDeniedException;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.global.context.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.global.exception.RequiredParamMissingException;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


import static com.dangbun.domain.checklist.response.status.ChecklistExceptionResponse.CHECKLIST_ACCESS_DENIED;
import static com.dangbun.global.response.status.BaseExceptionResponse.REQUIRED_PARAM_MISSING;

@Aspect
@Order(2)
@Component
@RequiredArgsConstructor
public class CheckChecklistMembershipAspect {

    private final ChecklistRepository checklistRepository;
    private final SecuritySupport securitySupport;
    private final RequestParamResolver requestParamResolver;
    private final AnnotationResolver annotationResolver;

    @Around("@within(com.dangbun.global.aop.CheckChecklistMembership) || @annotation(com.dangbun.global.aop.CheckChecklistMembership)")
    public Object validate(ProceedingJoinPoint joinPoint) throws Throwable {
        securitySupport.currentUserOrThrow();

        CheckChecklistMembership checkChecklistMembership = annotationResolver.resolve(joinPoint, CheckChecklistMembership.class);

        String checklistIdParamName = checkChecklistMembership.checklistIdParam();
        Long checklistId = requestParamResolver.resolveLong(joinPoint, checklistIdParamName);

        if (checklistId == null) {
            throw new RequiredParamMissingException(REQUIRED_PARAM_MISSING);
        }

        Member me = MemberContext.get();

        Checklist checklist;
        if (me.getRole() == MemberRole.MANAGER) {
            checklist = checklistRepository.findById(checklistId)
                    .orElseThrow(() -> new ChecklistAccessDeniedException(CHECKLIST_ACCESS_DENIED));
        } else {
            checklist = checklistRepository
                    .findByChecklistAndMemberId(checklistId, me.getMemberId())
                    .orElseThrow(() -> new ChecklistAccessDeniedException(CHECKLIST_ACCESS_DENIED));
        }

        try {
            ChecklistContext.set(checklist);
            return joinPoint.proceed();
        } finally {
            ChecklistContext.clear();
        }

    }

}
