package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.cleaningImage.application.port.out.CleaningImageCommandPort;
import com.dangbun.domain.cleaningImage.application.port.out.CleaningImageQueryPort;
import com.dangbun.domain.cleaningImage.domain.CleaningImage;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
class CleaningImagePersistenceAdapter implements CleaningImageQueryPort, CleaningImageCommandPort {

    private final SpringDataCleaningImageRepository cleaningImageRepository;
    private final CleaningImageMapper cleaningImageMapper;

    @Override
    public Optional<CleaningImage> findByChecklistId(Long checklistId) {
        return cleaningImageRepository.findByChecklist_ChecklistId(checklistId)
                .map(cleaningImageMapper::mapToDomainEntity);
    }

    @Override
    public boolean existsByChecklistId(Long checklistId) {
        return cleaningImageRepository.existsByChecklist_ChecklistId(checklistId);
    }

    @Override
    public CleaningImage save(CleaningImage cleaningImage) {
        CleaningImageJpaEntity entity = cleaningImageMapper.mapToJpaEntity(cleaningImage);
        CleaningImageJpaEntity saved = cleaningImageRepository.save(entity);
        return cleaningImageMapper.mapToDomainEntity(saved);
    }

    @Override
    public void deleteByChecklistId(Long checklistId) {
        cleaningImageRepository.deleteByChecklist_ChecklistId(checklistId);
    }
}
