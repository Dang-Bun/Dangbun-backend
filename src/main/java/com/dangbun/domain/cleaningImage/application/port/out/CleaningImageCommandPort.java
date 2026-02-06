package com.dangbun.domain.cleaningImage.application.port.out;

import com.dangbun.domain.cleaningImage.domain.CleaningImage;



public interface CleaningImageCommandPort {

    CleaningImage save(CleaningImage cleaningImage);

    void deleteByChecklistId(Long checklistId);
}
