package com.dangbun.domain.place.application.port.out;

import com.dangbun.domain.place.domain.Place;

public interface PlaceCommandPort {
    Place save(Place place);

    void delete(Place place);

    void updateTime(UpdatePlaceTimeCommand command);
}
