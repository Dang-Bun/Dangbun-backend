package com.dangbun.domain.place.adapter.out.persistence;

import com.dangbun.domain.place.application.port.out.PlaceCommandPort;
import com.dangbun.domain.place.application.port.out.PlaceQueryPort;
import com.dangbun.domain.place.application.port.out.UpdatePlaceTimeCommand;
import com.dangbun.domain.place.domain.Place;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 테스트용 인메모리 Place 저장소
 * Mock 대신 실제 동작을 시뮬레이션
 */
public class FakePlaceRepository implements PlaceCommandPort, PlaceQueryPort {

    private final Map<Long, Place> storage = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Place save(Place place) {
        if (place.getPlaceId() == null) {
            Long newId = idGenerator.getAndIncrement();
            Place saved = new Place(
                    new Place.PlaceId(newId),
                    place.getName(),
                    place.getCategory(),
                    place.getCategoryName(),
                    place.getInviteCode(),
                    place.getStartTime(),
                    place.getEndTime(),
                    place.getIsToday()
            );
            storage.put(newId, saved);
            return saved;
        } else {
            storage.put(place.getPlaceId().value(), place);
            return place;
        }
    }

    @Override
    public void delete(Place place) {
        storage.remove(place.getPlaceId().value());
    }

    @Override
    public void updateTime(UpdatePlaceTimeCommand command) {
        Place place = storage.get(command.placeId());
        if (place != null) {
            place.setTime(command.startTime(), command.endTime(), command.isToday());
            storage.put(command.placeId(), place);
        }
    }

    @Override
    public Optional<Place> findById(Long placeId) {
        return Optional.ofNullable(storage.get(placeId));
    }

    @Override
    public Optional<Place> findByInviteCode(String inviteCode) {
        return storage.values().stream()
                .filter(p -> inviteCode.equals(p.getInviteCode()))
                .findFirst();
    }

    // 테스트 헬퍼 메서드
    public void clear() {
        storage.clear();
        idGenerator.set(1);
    }

    public int count() {
        return storage.size();
    }

    public List<Place> findAll() {
        return new ArrayList<>(storage.values());
    }
}
