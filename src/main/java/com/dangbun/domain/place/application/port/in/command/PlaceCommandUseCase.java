package com.dangbun.domain.place.application.port.in.command;

public interface PlaceCommandUseCase {
    Long createPlaceWithManager(CreatePlaceCommand command);

    String createInviteCode();

    Long joinPlaceRequest(JoinPlaceCommand command);

    void deletePlace(String placeName);

    void cancelRegister();

    UpdateTimeResult updateTime(UpdateTimeCommand command);

}
