package com.dangbun.domain.checklist.adapter.out.persistence;

import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class ChecklistMapper {

    private final CleaningRepository cleaningRepository;

    Checklist mapToDomainEntity(ChecklistJpaEntity jpaEntity) {
        return Checklist.withId(
                jpaEntity.getChecklistId(),
                jpaEntity.getCleaningJpaEntity().getCleaningId(),
                jpaEntity.getIsComplete(),
                jpaEntity.getCompleteMemberId(),
                jpaEntity.getCompleteTime(),
                jpaEntity.getCreatedAt(),
                jpaEntity.getUpdatedAt()
        );
    }

    ChecklistJpaEntity mapToJpaEntity(Checklist domain) {
        ChecklistJpaEntity.ChecklistJpaEntityBuilder builder = ChecklistJpaEntity.builder()
                .cleaningJpaEntity(cleaningRepository.getReferenceById(domain.getCleaningId()))
                .isComplete(domain.getIsComplete())
                .completeMemberId(domain.getCompleteMemberId())
                .completeTime(domain.getCompleteTime());

        if (domain.getChecklistId() != null) {
            builder.checklistId(domain.getChecklistId().value());
        }

        return builder.build();
    }
}
