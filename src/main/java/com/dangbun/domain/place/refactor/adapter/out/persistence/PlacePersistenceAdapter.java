package com.dangbun.domain.place.refactor.adapter.out.persistence;

import com.dangbun.domain.place.refactor.PersistenceAdapter;
import com.dangbun.domain.place.refactor.application.port.out.UpdatePlaceStatePort;
import com.dangbun.domain.place.refactor.domain.Place;
import lombok.RequiredArgsConstructor;

@PersistenceAdapter
@RequiredArgsConstructor
class PlacePersistenceAdapter implements UpdatePlaceStatePort {

    private final SpringDataPlaceRepository placeRepository;
    private final PlaceMapper placeMapper;

    @Override
    public Long creatPlace(Place place) {
        PlaceJpaEntity saved = placeRepository.save(placeMapper.mapToJpaEntity(place));
        return saved.getPlaceId();
    }

}
