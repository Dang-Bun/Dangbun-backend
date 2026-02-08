package com.dangbun.domain.cleaningImage.application.port.in.query;

public interface CleaningImageQuery {

    String getImageUrl(Long checklistId);

    boolean isImagePresent(Long checklistId);
}
