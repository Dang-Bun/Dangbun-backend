package com.dangbun.domain.place.service;

import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.duty.service.DutyService;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.member.service.MemberService;
import com.dangbun.domain.place.dto.request.DeletePlaceRequest;
import com.dangbun.domain.place.dto.request.PatchUpdateTimeRequest;
import com.dangbun.domain.place.dto.request.PostCreatePlaceRequest;
import com.dangbun.domain.place.dto.request.PostRegisterPlaceRequest;
import com.dangbun.domain.place.dto.response.PatchUpdateTimeResponse;
import com.dangbun.domain.place.dto.response.PostCreateInviteCodeResponse;
import com.dangbun.domain.place.dto.response.PostCreatePlaceResponse;
import com.dangbun.domain.place.dto.response.PostRegisterPlaceResponse;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.entity.PlaceCategory;
import com.dangbun.domain.place.exception.custom.InvalidInformationException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.exception.custom.InvalidPlaceNameException;
import com.dangbun.domain.place.exception.custom.InvalidTimeException;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

@Service
@RequiredArgsConstructor
public class PlaceCommandService {

    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PlaceRepository placeRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final DutyService dutyService;
    private final MemberService memberService;
    private final DutyRepository dutyRepository;

    @Transactional
    public PostCreatePlaceResponse createPlaceWithManager(Long userId, PostCreatePlaceRequest request) {


        String placeName = request.placeName();
        PlaceCategory category = request.category();
        String memberName = request.managerName();

        String categoryName = request.categoryName() == null ? null : request.categoryName();

        if(category!=PlaceCategory.ETC) categoryName = category.getDisplayName();

        Map<String, String> info = request.information();

        Place place = Place.builder()
                .name(placeName)
                .category(category)
                .categoryName(categoryName)
                .build();


        Place savedPlace = placeRepository.save(place);

        Member member = Member.builder()
                .name(memberName)
                .place(savedPlace)
                .information(info)
                .role(MemberRole.MANAGER)
                .status(true)
                .user(userRepository.findById(userId).orElseThrow(() -> new NoSuchUserException(NO_SUCH_USER)))
                .build();

        memberRepository.save(member);

        return PostCreatePlaceResponse.of(savedPlace.getPlaceId());
    }

    @Transactional
    public PostCreateInviteCodeResponse createInviteCode() {
        Place place = MemberContext.get().getPlace();
        String code = place.createCode(generateCode());

        return new PostCreateInviteCodeResponse(code);
    }

    @Transactional
    public PostRegisterPlaceResponse joinRequest(User user, PostRegisterPlaceRequest request) {


        Member tempMember = memberRepository.findFirstWithPlaceByInviteCode(request.inviteCode())
                .orElseThrow(() -> new InvalidInviteCodeException(INVALID_INVITE_CODE));

        Place place = tempMember.getPlace();

        if (!tempMember.getInformation().keySet().equals(request.information().keySet())) {
            throw new InvalidInformationException(INVALID_INFORMATION);
        }

        Member member = Member.builder()
                .user(user)
                .role(MemberRole.MEMBER)
                .status(false)
                .place(place)
                .name(request.name())
                .information(request.information())
                .build();

        memberRepository.save(member);

        return PostRegisterPlaceResponse.of(place.getPlaceId());
    }

    @Transactional
    public void deletePlace(DeletePlaceRequest request) {
        Member runner = MemberContext.get();

        Place place = runner.getPlace();
        if (!place.getName().equals(request.placeName())) {
            throw new InvalidPlaceNameException(INVALID_NAME);
        }

        List<Duty> duties = dutyRepository.findByPlace_PlaceId(place.getPlaceId());

        for (Duty duty : duties) {
            dutyService.deleteDuty(duty.getDutyId());
        }

        List<Member> members = memberRepository.findAllByPlace(place);
        for (Member member : members) {
            memberService.deleteMember(member);
        }

        placeRepository.delete(place);

    }

    @Transactional
    public void cancelRegister() {
        Member member = MemberContext.get();

        memberRepository.delete(member);
    }

    @Transactional
    public PatchUpdateTimeResponse updateTime(PatchUpdateTimeRequest request) {
        Place place = MemberContext.get().getPlace();

        if (request.isToday() && request.startTime().isAfter(request.endTime())) {
            throw new InvalidTimeException(INVALID_TIME);
        }

        place.setTime(request.startTime(), request.endTime(), request.isToday());

        return PatchUpdateTimeResponse.of(place, place.getIsToday());
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
