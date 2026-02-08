package com.dangbun.domain.cleaning.application.port.in.query;

import com.dangbun.domain.cleaning.domain.Cleaning;

import java.util.*;

/**
 * 테스트용 인메모리 CleaningsByPlaceQuery 구현
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakeCleaningsByPlaceQuery implements GetCleaningsByPlaceQuery {

    private final List<Cleaning> storage = new ArrayList<>();

    @Override
    public List<Cleaning> getCleaningsByPlaceId(Long placeId) {
        return storage.stream()
                .filter(c -> c.getPlaceId().equals(placeId))
                .toList();
    }

    public void addCleaning(Cleaning cleaning) {
        storage.add(cleaning);
    }

    public void clear() {
        storage.clear();
    }

    public int count() {
        return storage.size();
    }
}
