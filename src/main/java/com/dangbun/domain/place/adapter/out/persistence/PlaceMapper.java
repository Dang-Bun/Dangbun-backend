package com.dangbun.domain.place.adapter.out.persistence;

import com.dangbun.domain.place.domain.Place;
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

    public Place mapToDomainEntity(PlaceJpaEntity entity) {
        Place place = new Place(
                new Place.PlaceId(entity.getPlaceId()),
                entity.getName(),
                entity.getCategory(),
                entity.getCategoryName(),
                entity.getInviteCode(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getIsToday()
        );
        return place;
    }

}
