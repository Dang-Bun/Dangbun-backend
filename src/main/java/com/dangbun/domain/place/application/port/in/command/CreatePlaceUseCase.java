package com.dangbun.domain.place.application.port.in.command;


public interface CreatePlaceUseCase {
    public Long createPlaceWithManager(CreatePlaceCommand command);
}
