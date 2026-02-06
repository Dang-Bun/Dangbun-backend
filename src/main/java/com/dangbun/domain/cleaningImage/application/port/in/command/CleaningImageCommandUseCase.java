package com.dangbun.domain.cleaningImage.application.port.in.command;

import java.util.Map;

public interface CleaningImageCommandUseCase {

    Map<String, String> generateUploadUrl(String filename, String contentType, Long checklistId);

    void saveImage(Long checklistId, String s3Key);

    void deleteByChecklistId(Long checklistId);

    void deleteS3File(Long checklistId);
}
