package com.dangbun.domain.cleaningImage.application.port.in.command;

import java.util.*;

/**
 * 테스트용 인메모리 CleaningImageCommandUseCase 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningImageCommandUseCase implements CleaningImageCommandUseCase {

    private final Map<Long, String> storage = new HashMap<>();
    private final List<Long> deleteHistory = new ArrayList<>();
    private final List<Long> deleteS3History = new ArrayList<>();

    @Override
    public Map<String, String> generateUploadUrl(String filename, String contentType, Long checklistId) {
        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", "https://fake-s3.com/upload/" + filename);
        result.put("s3Key", "fake-key/" + filename);
        return result;
    }

    @Override
    public void saveImage(Long checklistId, String s3Key) {
        storage.put(checklistId, s3Key);
    }

    @Override
    public void deleteByChecklistId(Long checklistId) {
        storage.remove(checklistId);
        deleteHistory.add(checklistId);
    }

    @Override
    public void deleteS3File(Long checklistId) {
        storage.remove(checklistId);
        deleteS3History.add(checklistId);
    }

    public String getS3KeyByChecklistId(Long checklistId) {
        return storage.get(checklistId);
    }

    public List<Long> getDeleteHistory() {
        return new ArrayList<>(deleteHistory);
    }

    public List<Long> getDeleteS3History() {
        return new ArrayList<>(deleteS3History);
    }

    public void clear() {
        storage.clear();
        deleteHistory.clear();
        deleteS3History.clear();
    }

    public int count() {
        return storage.size();
    }
}
