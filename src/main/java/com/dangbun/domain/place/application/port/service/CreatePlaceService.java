package com.dangbun.domain.place.application.port.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.place.application.port.in.command.CreatePlaceCommand;
import com.dangbun.domain.place.application.port.in.command.CreatePlaceUseCase;
import com.dangbun.domain.place.application.port.out.UpdatePlaceStatePort;
import com.dangbun.domain.place.domain.Place;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
@UseCase
public class CreatePlaceService implements CreatePlaceUseCase {

    private final UpdatePlaceStatePort updatePlaceStatePort;

    @Override
    @Transactional
    public Long createPlaceWithManager(CreatePlaceCommand command) {
        Place place = new Place(
                null,
                command.getPlaceName(),
                command.getCategory(),
                command.getCategoryName(),
                null,
                null,
                null,
                null);
        Long placeId = updatePlaceStatePort.creatPlace(place);

        return placeId;
    }

}
