package com.dangbun.domain.cleaningImage.repository;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.cleaningImage.entity.CleaningImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CleaningImageRepository extends JpaRepository<CleaningImage, Long> {

    Optional<CleaningImage> getByChecklist_ChecklistId(Long checklistChecklistId);

    void deleteByChecklist_ChecklistId(Long checklistChecklistId);
}
