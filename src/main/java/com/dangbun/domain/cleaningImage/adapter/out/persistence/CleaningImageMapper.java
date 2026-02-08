package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import com.dangbun.domain.checklist.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.adapter.out.persistence.SpringDataChecklistRepository;
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
    private final SpringDataChecklistRepository checklistRepository;

    public CleaningImageJpaEntity mapToJpaEntity(CleaningImage cleaningImage) {
        ChecklistJpaEntity checklistJpaEntity = checklistRepository.findById(cleaningImage.getChecklistId())
                .orElseThrow(() -> new IllegalArgumentException("Checklist not found: " + cleaningImage.getChecklistId()));

        CleaningImageJpaEntity.CleaningImageJpaEntityBuilder builder = CleaningImageJpaEntity.builder()
                .s3Key(cleaningImage.getS3Key())
                .uploader(cleaningImage.getUploader())
                .checklistJpaEntity(checklistJpaEntity);

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
                entity.getChecklistJpaEntity().getChecklistId()
        );
    }
}
