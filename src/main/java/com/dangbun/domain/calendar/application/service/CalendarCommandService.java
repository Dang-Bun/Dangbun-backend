package com.dangbun.domain.calendar.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.PatchUpdateChecklistToCompleteResponse;
import com.dangbun.domain.calendar.application.port.in.command.CalendarCommandUseCase;
import com.dangbun.domain.checklist.application.port.in.command.ChecklistForCalendarUseCase;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.application.port.in.query.GetMemberForCalendarQuery;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CalendarCommandService implements CalendarCommandUseCase {

    private final ChecklistForCalendarUseCase checklistForCalendarUseCase;
    private final GetMemberForCalendarQuery getMemberForCalendarQuery;

    @Override
    public PatchUpdateChecklistToCompleteResponse finishChecklist(Long checklistId) {
        MemberJpaEntity me = MemberContext.get();

        ChecklistForCalendarUseCase.CompleteResult result = checklistForCalendarUseCase.completeChecklist(checklistId, me.getMemberId());

        return PatchUpdateChecklistToCompleteResponse.of(me.getName(), result.completeTime());
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        checklistForCalendarUseCase.deleteChecklist(checklistId);
    }
}
