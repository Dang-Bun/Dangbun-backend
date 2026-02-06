package com.dangbun.domain.cleaningImage.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCleaningImageRepository extends JpaRepository<CleaningImageJpaEntity, Long> {

    Optional<CleaningImageJpaEntity> findByChecklist_ChecklistId(Long checklistId);

    boolean existsByChecklist_ChecklistId(Long checklistId);

    void deleteByChecklist_ChecklistId(Long checklistId);
}
