package com.dangbun.domain.place.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.duty.service.DutyService;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.exception.custom.InvalidRoleException;
import com.dangbun.domain.member.exception.custom.MemberNotFoundException;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.member.service.MemberService;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.domain.place.dto.request.*;
import com.dangbun.domain.place.dto.response.*;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.entity.PlaceCategory;
import com.dangbun.domain.place.exception.custom.*;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.exception.custom.UserNotFoundException;
import com.dangbun.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.member.response.status.MemberExceptionResponse.INVALID_ROLE;
import static com.dangbun.domain.member.response.status.MemberExceptionResponse.NO_SUCH_MEMBER;
import static com.dangbun.domain.place.dto.response.GetPlaceListResponse.PlaceDto;
import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

@Service
@RequiredArgsConstructor
@Transactional
public class PlaceService {


    private final PlaceRepository placeRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;


    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();
    private final MemberDutyRepository memberDutyRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final ChecklistRepository checkListRepository;
    private final DutyRepository dutyRepository;
    private final DutyService dutyService;
    private final MemberService memberService;
    private final NotificationReceiverRepository notificationReceiverRepository;

    @Transactional(readOnly = true)
    public GetPlaceListResponse getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);

        List<PlaceDto> placeDtos = new ArrayList<>();
        for (Member member : members) {
            if (!member.getStatus()) {
                Place place = member.getPlace();
                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(), place.getCategory(), place.getCategoryName(),null,null, null, null));
            } else {
                Place place = member.getPlace();

                List<MemberCleaning> memberCleanings = memberCleaningRepository.findAllByMember(member);
                Integer totalCleaning = memberCleanings.size();
                Integer endCleaning = 0;
                for (MemberCleaning memberCleaning : memberCleanings) {
                    Cleaning cleaning = memberCleaning.getCleaning();
                    LocalDate now = LocalDate.now();
                    LocalDateTime start = now.atStartOfDay();
                    LocalDateTime end = now.plusDays(1).atStartOfDay();
                    if(checkListRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaning)){
                        endCleaning++;
                    }
                }

                Integer notifyNumber = notificationReceiverRepository.countUnreadByMemberId(member.getMemberId());


                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(),place.getCategory(),place.getCategoryName(), totalCleaning, endCleaning, member.getRole().getDisplayName(), notifyNumber));
            }
        }

        return GetPlaceListResponse.of(placeDtos);
    }

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
                .user(userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(NO_SUCH_USER)))
                .build();

        memberRepository.save(member);

        return PostCreatePlaceResponse.of(savedPlace.getPlaceId());
    }

    public PostCreateInviteCodeResponse createInviteCode(Long userId, Long placeId) {
        Member manager = getManager(userId, placeId);

        Place place = manager.getPlace();
        String code = place.createCode(generateCode());

        return new PostCreateInviteCodeResponse(code);
    }


    @Transactional(readOnly = true)
    public PostCheckInviteCodeResponse checkInviteCode(User user, PostCheckInviteCodeRequest request) {
        Place place = placeRepository.findByInviteCode(request.inviteCode());
        if (place == null) {
            throw new InvalidInviteCodeException(NO_SUCH_INVITE_CODE);
        }
        if (memberRepository.findByPlaceAndUser(place, user).isPresent()) {
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }
        Member member = memberRepository.findFirstByPlace(place);
        Set<String> information = member.getInformation().keySet();
        List<String> iList = information.stream().toList();

        return PostCheckInviteCodeResponse.of(place.getPlaceId(), iList);
    }

    public static String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    public PostRegisterPlaceResponse joinRequest(User user, PostRegisterPlaceRequest request) {


        Member tempMember = memberRepository.findFirstWithPlaceByInviteCode(request.inviteCode())
                .orElseThrow(() -> new InvalidInviteCodeException(NO_SUCH_INVITE_CODE));

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

    @Transactional(readOnly = true)
    public GetPlaceResponse getPlace(User user, Long placeId) {

        Place place = placeRepository.findById(placeId)
                .orElseThrow(() -> new NoSuchPlaceException(NO_SUCH_PLACE));

        Member member = memberRepository.findByUser_UserIdAndPlace_PlaceId(user.getUserId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));

        if (!member.getStatus()) {
            return new GetPlaceResponse(member.getMemberId(), placeId, place.getName(), place.getCategory(),place.getCategoryName(),null, null);
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);

        List<MemberCleaning> memberCleanings = new ArrayList<>();
        for (MemberDuty memberDuty : memberDuties) {
            memberCleanings.addAll(memberCleaningRepository.findAllByMember(memberDuty.getMember()));
        }

        Map<MemberDuty, List<Checklist>> cleaningMap = memberDuties.stream()
                .collect(Collectors.toMap(
                        md -> md,
                        md -> checkListRepository.findWithCleaningByDutyId(md.getDuty().getDutyId())
                ));


        return GetPlaceResponse.of(user, place, cleaningMap, memberCleanings);
    }

    public void deletePlace(User user, Long placeId, DeletePlaceRequest request) {
        Member runner = memberRepository.findWithPlaceByUserIdAndPlaceId(user.getUserId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));

        if (!runner.getRole().equals(MemberRole.MANAGER)) {
            throw new InvalidRoleException(INVALID_ROLE);
        }

        Place place = runner.getPlace();

        if(!place.getName().equals(request.placeName())){
            throw new InvalidPlaceNameException(INVALID_NAME);
        }

        List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);
        for (Duty duty : duties) {
            dutyService.deleteDuty(duty.getDutyId());
        }

        List<Member> members = memberRepository.findAllByPlace(place);
        for (Member member : members) {
            memberService.deleteMember(member);
        }


        placeRepository.delete(place);

    }

    public void cancelRegister(User user, Long placeId) {
        Member member = memberRepository.findByUser_UserIdAndPlace_PlaceId(user.getUserId(), placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));

        memberRepository.delete(member);
    }

    public void updateTime(User user, Long placeId, PatchUpdateTimeRequest request) {
        Member manager = getManager(user.getUserId(), placeId);
        if (request.startTime().isAfter(request.endTime()) && request.isToday()) {
            throw new InvalidTimeException(INVALID_TIME);
        }
        Place place = manager.getPlace();
        place.setTime(request.startTime(), request.endTime());
    }

    public GetDutiesProgressResponse getDutiesProgress(User user, Long placeId) {
        Member manager = getManager(user.getUserId(), placeId);

        List<DutyProgressDto> dutyDtos = dutyRepository.findDutyProgressByPlaceToday(placeId);
        return GetDutiesProgressResponse.of(dutyDtos);

    }

    private Member getManager(Long userId, Long placeId) {
        Member member = memberRepository.findWithPlaceByUserIdAndPlaceId(userId, placeId)
                .orElseThrow(() -> new MemberNotFoundException(NO_SUCH_MEMBER));
        if (member.getRole().equals(MemberRole.MEMBER)) {
            throw new InvalidRoleException(INVALID_ROLE);
        }
        return member;
    }


}
