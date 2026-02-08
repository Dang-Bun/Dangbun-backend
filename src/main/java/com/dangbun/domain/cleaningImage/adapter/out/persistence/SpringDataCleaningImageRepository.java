package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCleaningImageRepository extends JpaRepository<CleaningImageJpaEntity, Long> {

    Optional<CleaningImageJpaEntity> findByChecklistJpaEntity_ChecklistId(Long checklistId);

    boolean existsByChecklistJpaEntity_ChecklistId(Long checklistId);

    void deleteByChecklistJpaEntity_ChecklistId(Long checklistId);
}
