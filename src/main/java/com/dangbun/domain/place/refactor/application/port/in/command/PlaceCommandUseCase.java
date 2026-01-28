package com.dangbun.domain.place.refactor.application.port.in.command;

import java.util.Map;

public interface PlaceCommandUseCase {
    Long createPlaceWithManager(CreatePlaceCommand command);

    String createInviteCode();

    Long joinPlaceRequest(JoinPlaceCommand command);

    void deletePlace(String placeName);

    void cancelRegister();

    UpdateTimeResult updateTime(UpdateTimeCommand command);

}
