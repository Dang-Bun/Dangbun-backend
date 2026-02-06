package com.dangbun.domain.cleaningImage.application.port.out;

import com.dangbun.domain.cleaningImage.domain.CleaningImage;

import java.util.Optional;

public interface CleaningImageQueryPort {

    Optional<CleaningImage> findByChecklistId(Long checklistId);

    boolean existsByChecklistId(Long checklistId);
}
