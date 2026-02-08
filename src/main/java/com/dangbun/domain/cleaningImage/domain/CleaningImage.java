package com.dangbun.domain.cleaningImage.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CleaningImage {

    private final CleaningImageId cleaningImageId;
    private final String s3Key;
    private final String uploader;
    private final Long checklistId;

    public static CleaningImage withoutId(String s3Key, String uploader, Long checklistId) {
        return new CleaningImage(null, s3Key, uploader, checklistId);
    }

    public static CleaningImage withId(CleaningImageId cleaningImageId, String s3Key, String uploader, Long checklistId) {
        return new CleaningImage(cleaningImageId, s3Key, uploader, checklistId);
    }

    public record CleaningImageId(Long value) {
    }
}
