package com.dangbun.domain.place.refactor.application.port.service;

import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.place.refactor.UseCase;
import com.dangbun.domain.place.refactor.application.port.in.CreatePlaceCommand;
import com.dangbun.domain.place.refactor.application.port.in.CreatePlaceUseCase;
import com.dangbun.domain.place.refactor.application.port.out.UpdatePlaceStatePort;
import com.dangbun.domain.place.refactor.domain.Place;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;

import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

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
