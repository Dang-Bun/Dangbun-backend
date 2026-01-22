package com.dangbun.domain.place.refactor.application.port.in;


import com.dangbun.domain.place.refactor.domain.Place;

public interface CreatePlaceUseCase {
    public Long createPlaceWithManager(CreatePlaceCommand command);
}
