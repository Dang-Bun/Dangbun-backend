package com.dangbun.domain.calendar.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.PatchUpdateChecklistToCompleteResponse;
import com.dangbun.domain.calendar.application.port.in.command.CalendarCommandUseCase;
import com.dangbun.domain.checklist.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.adapter.out.persistence.SpringDataChecklistRepository;
import com.dangbun.domain.cleaningImage.application.port.in.command.CleaningImageCommandUseCase;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@UseCase
@RequiredArgsConstructor
@Transactional
public class CalendarCommandService implements CalendarCommandUseCase {

    /*
     * TODO: Checklist 도메인 헥사고날 아키텍처 전환 시 수정
     * ChecklistRepository -> ChecklistCommandPort
     */
    private final SpringDataChecklistRepository checklistRepository;
    private final CleaningImageCommandUseCase cleaningImageCommandUseCase;

    @Override
    public PatchUpdateChecklistToCompleteResponse finishChecklist(Long checklistId) {
        MemberJpaEntity me = MemberContext.get();

        ChecklistJpaEntity checklistJpaEntity = checklistRepository.findById(checklistId)
                .orElseThrow();

        checklistJpaEntity.completeChecklist(me.getMemberId());

        return PatchUpdateChecklistToCompleteResponse.of(me.getName(), LocalTime.now());
    }

    @Override
    public void deleteChecklist(Long checklistId) {
        cleaningImageCommandUseCase.deleteS3File(checklistId);
        checklistRepository.deleteById(checklistId);
    }
}
