package com.dangbun.domain.place.refactor.application.port.service;

import com.dangbun.domain.member.MemberCommandPort;
import com.dangbun.domain.member.GetMemberByInviteCodePort;
import com.dangbun.domain.member.Member;
import com.dangbun.domain.member.entity.MemberJpaEntity;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.place.original.dto.response.PatchUpdateTimeResponse;
import com.dangbun.domain.place.original.entity.PlaceCategory;
import com.dangbun.domain.place.original.exception.custom.InvalidInformationException;
import com.dangbun.domain.place.original.exception.custom.InvalidPlaceNameException;
import com.dangbun.domain.place.original.exception.custom.InvalidTimeException;
import com.dangbun.domain.place.refactor.application.port.in.command.*;
import com.dangbun.domain.place.refactor.application.port.out.PlaceCommandPort;
import com.dangbun.domain.place.refactor.application.port.out.UpdatePlaceTimeCommand;
import com.dangbun.domain.place.refactor.domain.Place;
import com.dangbun.global.context.MemberContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;

import static com.dangbun.domain.place.original.response.status.PlaceExceptionResponse.*;
import static org.reflections.Reflections.log;

@RequiredArgsConstructor
@Service
public class PlaceCommandService implements PlaceCommandUseCase {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PlaceCommandPort placeCommandPort;
    private final MemberCommandPort memberCommandPort;
    private final GetMemberByInviteCodePort getMemberByInviteCodePort;

    @Override
    @Transactional
    public Long createPlaceWithManager(CreatePlaceCommand command) {

        Long userId = command.getUserId();
        String placeName = command.getPlaceName();
        PlaceCategory category = command.getCategory();
        String memberName = command.getManagerName();
        String categoryName = command.getCategoryName() == null ? null : command.getCategoryName();

        if (category != PlaceCategory.ETC) categoryName = category.getDisplayName();

        Map<String, String> info = command.getInformation();

        Place place = Place.withoutId(placeName, category, categoryName);


        place.createCode(generateCode());

        Place savedPlace = placeCommandPort.save(place);


        Member manager = Member.builder()
                .name(memberName)
                .placeId(savedPlace.getPlaceId().value())
                .information(info)
                .role(MemberRole.MANAGER)
                .status(true)
                .userId(userId)
                .build();


        memberCommandPort.save(manager);

        return savedPlace.getPlaceId().value();
    }

    @Override
    @Transactional
    public String createInviteCode() {
        /*
            Todo Jpa entity 제거
         */
        com.dangbun.domain.place.original.entity.Place place = MemberContext.get().getPlace();
        Place domainEntity = com.dangbun.domain.place.original.entity.Place.toDomainEntity(place);
        String code = domainEntity.createCode(generateCode());
        placeCommandPort.save(domainEntity);
        return code;
    }

    @Override
    @Transactional
    public Long joinPlaceRequest(JoinPlaceCommand command) {
        Member tempMember = getMemberByInviteCodePort.getMember(command.inviteCode());
        Long placeId = tempMember.getPlaceId();

        if (!tempMember.getInformation().keySet().equals(command.information().keySet())) {
            throw new InvalidInformationException(INVALID_INFORMATION);
        }

        Member member = Member.builder()
                .name(command.name())
                .placeId(placeId)
                .information(command.information())
                .role(MemberRole.WAITING)
                .status(true)
                .userId(command.userId())
                .build();


        log.info(member.getRole().toString());
        memberCommandPort.save(member);

        return placeId;
    }

    @Override
    public void deletePlace(String placeName) {

        /*
            Todo Jpa entity 제거
         */

        com.dangbun.domain.place.original.entity.Place place = MemberContext.get().getPlace();
        Place domainEntity = com.dangbun.domain.place.original.entity.Place.toDomainEntity(place);

        if (!domainEntity.getName().equals(placeName)) {
            throw new InvalidPlaceNameException(INVALID_NAME);
        }

        placeCommandPort.delete(domainEntity);
    }

    @Override
    public void cancelRegister() {

        //Todo delete
        MemberJpaEntity jpaMember = MemberContext.get();

        Member member = Member.withId(
                jpaMember.getMemberId(),
                jpaMember.getRole(),
                jpaMember.getName(),
                jpaMember.getStatus(),
                jpaMember.getInformation(),
                jpaMember.getPlace().getPlaceId(),
                jpaMember.getUser().getUserId());

        memberCommandPort.delete(member);
    }

    @Override
    public UpdateTimeResult updateTime(UpdateTimeCommand command) {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        if (command.isToday() && command.startTime().isAfter(command.endTime())) {
            throw new InvalidTimeException(INVALID_TIME);
        }

        placeCommandPort.updateTime(UpdatePlaceTimeCommand.of(placeId, command.startTime(), command.endTime(), command.isToday()));
        return UpdateTimeResult.of(command.startTime(), command.endTime(), command.isToday());
    }


    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
