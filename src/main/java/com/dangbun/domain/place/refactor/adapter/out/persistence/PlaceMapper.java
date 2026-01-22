package com.dangbun.domain.place.refactor.adapter.out.persistence;

import com.dangbun.domain.place.refactor.domain.Place;
import org.springframework.stereotype.Component;

@Component
class PlaceMapper {

    public PlaceJpaEntity mapToJpaEntity(Place place) {
        return new PlaceJpaEntity(
                place.getName(),
                place.getCategory(),
                place.getCategoryName()
        );
    }
}
