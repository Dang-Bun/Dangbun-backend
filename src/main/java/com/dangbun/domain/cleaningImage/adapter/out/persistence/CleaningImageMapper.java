package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaningImage.domain.CleaningImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleaningImageMapper {

    /*
     * TODO: Checklist 도메인 헥사고날 아키텍처 전환 시 수정
     * ChecklistRepository -> ChecklistQueryPort 또는 SpringDataChecklistRepository
     */
    private final ChecklistRepository checklistRepository;

    public CleaningImageJpaEntity mapToJpaEntity(CleaningImage cleaningImage) {
        Checklist checklist = checklistRepository.findById(cleaningImage.getChecklistId())
                .orElseThrow(() -> new IllegalArgumentException("Checklist not found: " + cleaningImage.getChecklistId()));

        CleaningImageJpaEntity.CleaningImageJpaEntityBuilder builder = CleaningImageJpaEntity.builder()
                .s3Key(cleaningImage.getS3Key())
                .uploader(cleaningImage.getUploader())
                .checklist(checklist);

        if (cleaningImage.getCleaningImageId() != null) {
            builder.cleaningImageId(cleaningImage.getCleaningImageId().value());
        }

        return builder.build();
    }

    public CleaningImage mapToDomainEntity(CleaningImageJpaEntity entity) {
        return CleaningImage.withId(
                new CleaningImage.CleaningImageId(entity.getCleaningImageId()),
                entity.getS3Key(),
                entity.getUploader(),
                entity.getChecklist().getChecklistId()
        );
    }
}
