package com.dangbun.domain.place.refactor.application.port.in.command;


public interface CreatePlaceUseCase {
    public Long createPlaceWithManager(CreatePlaceCommand command);
}
