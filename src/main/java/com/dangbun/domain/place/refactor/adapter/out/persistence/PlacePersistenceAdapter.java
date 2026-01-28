package com.dangbun.domain.place.refactor.adapter.out.persistence;

import com.dangbun.domain.place.refactor.PersistenceAdapter;
import com.dangbun.domain.place.refactor.application.port.out.PlaceCommandPort;
import com.dangbun.domain.place.refactor.application.port.out.UpdatePlaceStatePort;
import com.dangbun.domain.place.refactor.application.port.out.UpdatePlaceTimeCommand;
import com.dangbun.domain.place.refactor.domain.Place;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
class PlacePersistenceAdapter implements UpdatePlaceStatePort, PlaceCommandPort {

    private final SpringDataPlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    @Override
    public Long creatPlace(Place place) {
        PlaceJpaEntity saved = placeRepository.save(placeMapper.mapToJpaEntity(place));
        return saved.getPlaceId();
    }

    @Override
    public Place save(Place place) {
        PlaceJpaEntity placeJpaEntity = placeMapper.mapToJpaEntity(place);
        PlaceJpaEntity saved = placeRepository.save(placeJpaEntity);
        return placeMapper.mapToDomainEntity(saved);
    }

    @Override
    public void delete(Place place) {
        PlaceJpaEntity placeJpaEntity = placeMapper.mapToJpaEntity(place);
        placeRepository.delete(placeJpaEntity);
    }

    @Override
    public void updateTime(UpdatePlaceTimeCommand command) {
        PlaceJpaEntity placeJpaEntity = placeRepository.findById(command.placeId()).get();

        placeJpaEntity.setTime(command.startTime(), command.endTime(), command.isToday());
    }
}
