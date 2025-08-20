package com.dangbun.domain.place.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
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
import com.dangbun.domain.user.exception.custom.NoSuchUserException;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.place.dto.response.GetPlaceListResponse.PlaceDto;
import static com.dangbun.domain.place.dto.response.GetPlaceResponse.*;
import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.*;
import static com.dangbun.domain.user.response.status.UserExceptionResponse.NO_SUCH_USER;

@Slf4j
@Service
@RequiredArgsConstructor
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
    private final NotificationReceiverRepository notificationReceiverRepository;
    private final CleaningRepository cleaningRepository;

    @Transactional(readOnly = true)
    public GetPlaceListResponse getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);

        List<PlaceDto> placeDtos = new ArrayList<>();
        for (Member member : members) {
            if (!member.getStatus()) {
                Place place = member.getPlace();
                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(), place.getCategory(), place.getCategoryName(), null, null, null, null));
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
                    if (checkListRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaning)) {
                        endCleaning++;
                    }
                }

                Integer notifyNumber = notificationReceiverRepository.countUnreadByMemberId(member.getMemberId());


                placeDtos.add(PlaceDto.of(place.getPlaceId(), place.getName(), place.getCategory(), place.getCategoryName(), totalCleaning, endCleaning, member.getRole().getDisplayName(), notifyNumber));
            }
        }

        return GetPlaceListResponse.of(placeDtos);
    }

    @Transactional
    public PostCreatePlaceResponse createPlaceWithManager(Long userId, PostCreatePlaceRequest request) {


        String placeName = request.placeName();
        PlaceCategory category = request.category();
        String memberName = request.managerName();

        String categoryName = request.categoryName() == null ? null : request.categoryName();

        if (category != PlaceCategory.ETC) categoryName = category.getDisplayName();

        Map<String, String> info = request.information();

        Place place = Place.builder()
                .name(placeName)
                .category(category)
                .categoryName(categoryName)
                .build();

        place.createCode(generateCode());

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


    @Transactional(readOnly = true)
    public PostCheckInviteCodeResponse checkInviteCode(User user, PostCheckInviteCodeRequest request) {
        Place place = placeRepository.findByInviteCode(request.inviteCode());
        if (place == null) {
            throw new InvalidInviteCodeException(INVALID_INVITE_CODE);
        }
        if (memberRepository.findByPlaceAndUser(place, user).isPresent()) {
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }
        Member member = memberRepository.findFirstByPlace(place);
        Set<String> information = member.getInformation().keySet();
        List<String> iList = information.stream().toList();

        return PostCheckInviteCodeResponse.of(place.getPlaceId(), iList);
    }


    @Transactional
    public PostRegisterPlaceResponse joinRequest(User user, PostRegisterPlaceRequest request) {


        Member tempMember = memberRepository.findWithPlaceByInviteCode(request.inviteCode()).stream().findAny()
                .orElseThrow(() -> new InvalidInviteCodeException(INVALID_INVITE_CODE));

        Place place = tempMember.getPlace();

        if (!tempMember.getInformation().keySet().equals(request.information().keySet())) {
            throw new InvalidInformationException(INVALID_INFORMATION);
        }

        Member member = Member.builder()
                .user(user)
                .role(MemberRole.WAITING)
                .status(false)
                .place(place)
                .name(request.name())
                .information(request.information())
                .build();
        log.info(member.getRole().toString());
        memberRepository.save(member);

        return PostRegisterPlaceResponse.of(place.getPlaceId());
    }

    @Transactional(readOnly = true)
    public GetPlaceResponse getPlace() {
        Member me = MemberContext.get();
        Place place = me.getPlace();
        Long placeId = place.getPlaceId();

        List<DutyDto> dutyDtos = null;


        if (!me.getStatus() || me.getRole() == MemberRole.WAITING) {
            return new GetPlaceResponse(me.getMemberId(), placeId, place.getName(), place.getCategory(), place.getCategoryName(), null, null);
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);

        List<MemberCleaning> memberCleanings = new ArrayList<>();

        for (MemberDuty memberDuty : memberDuties) {
            memberCleanings.addAll(memberCleaningRepository.findAllByMember(memberDuty.getMember()));
        }

        if (me.getRole() == MemberRole.MANAGER) {
            List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);

            Map<Duty, List<Checklist>> checklistMap = duties.stream()
                    .collect(Collectors.toMap(
                            d -> d,
                            d -> {
                                List<Checklist> rslt = filterChecklist(d, place);
                                return rslt;
                            }
                    ));

            dutyDtos = createManagerDutyDtos(placeId, checklistMap, memberCleanings );
        }

        List<Duty> duties = memberDuties.stream().map(MemberDuty::getDuty).distinct().toList();

        Map<Duty, List<Checklist>> checklistMap = duties.stream()
                .collect(Collectors.toMap(
                        d -> d,
                        d -> {
                            List<Checklist> rslt = filterChecklist(d, place);
                            return rslt;
                        }
                ));

        if (me.getRole() == MemberRole.MEMBER) {
            dutyDtos = createDutyDtos(me, checklistMap, memberCleanings);
        }

        return of(me.getMemberId(), placeId, place.getName(), place.getCategory(), place.getCategoryName(), place.getEndTime(), dutyDtos);
    }




    @Transactional
    public void deletePlace(DeletePlaceRequest request) {
        Member runner = MemberContext.get();

        Place place = runner.getPlace();
        if (!place.getName().equals(request.placeName())) {
            throw new InvalidPlaceNameException(INVALID_NAME);
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

    @Transactional(readOnly = true)
    public GetDutiesProgressResponse getDutiesProgress() {
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<DutyProgressDto> dutyDtos = dutyRepository.findDutyProgressByPlaceToday(placeId);
        return GetDutiesProgressResponse.of(dutyDtos);

    }

    @Transactional(readOnly = true)
    public GetTimeResponse getTimeAndIsToday() {
        Place place = MemberContext.get().getPlace();

        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();
        boolean isToday = place.getIsToday();

        return GetTimeResponse.of(startTime, endTime, isToday);
    }

    @Transactional(readOnly = true)
    public GetPlaceInvitedCodeResponse getInviteCode() {
        Place place = MemberContext.get().getPlace();
        String code = place.getInviteCode();

        if (code == null || code.isBlank()) {
            throw new InviteCodeNotExistsException(INVITE_CODE_NOT_EXISTS);
        }
        return GetPlaceInvitedCodeResponse.of(code);
    }

    /// without transaction

    private String generateCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

    private List<DutyDto> createDutyDtos(Member me, Map<Duty, List<Checklist>> checklistMap, List<MemberCleaning> memberCleanings) {
        List<DutyDto> duties = new ArrayList<>();

        for (Map.Entry<Duty, List<Checklist>> dc : checklistMap.entrySet()) {
            Duty duty = dc.getKey();
            List<CheckListDto> checkListDtos = new ArrayList<>();

            for (Checklist checkList : dc.getValue()) {
                Cleaning cleaning = checkList.getCleaning();

                List<Member> members = new ArrayList<>();
                boolean containsMe = false;

                for (MemberCleaning mc : memberCleanings) {
                    if (mc.getCleaning().equals(cleaning)) {
                        Member m = mc.getMember();
                        members.add(m);
                        if (m.equals(me)) {
                            containsMe = true;
                        }
                    }
                }

                if (containsMe) {
                    checkListDtos.add(CheckListDto.of(checkList, members));
                }
            }

            if (!checkListDtos.isEmpty()) {
                duties.add(DutyDto.of(duty.getName(), checkListDtos));
            }
        }
        return duties;
    }


    private List<DutyDto> createManagerDutyDtos(Long placeId, Map<Duty, List<Checklist>> checklistMap, List<MemberCleaning> memberCleanings) {
        List<DutyDto> duties = new ArrayList<>();
        for (Map.Entry<Duty, List<Checklist>> dc : checklistMap.entrySet()) {
            Duty duty = dc.getKey();

            List<CheckListDto> checkListDtos = new ArrayList<>();
            for (Checklist checkList : dc.getValue()) {
                Cleaning cleaning = checkList.getCleaning();

                List<Member> members = new ArrayList<>();

                for (MemberCleaning mc : memberCleanings) {
                    if (mc.getCleaning().equals(cleaning)) {
                        Member m = mc.getMember();
                        members.add(m);
                    }
                }

                checkListDtos.add(CheckListDto.of(checkList, members));

            }

            if (!checkListDtos.isEmpty()) {
                duties.add(DutyDto.of(duty.getName(), checkListDtos));
            }
        }

        return duties;
    }

    private List<Checklist> filterChecklist(Duty duty, Place place) {

        List<Checklist> rslt = new ArrayList<>();

        Boolean isToday = place.getIsToday();

        LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();


        List<Checklist> checklists = checkListRepository.findWithCleaningByDutyId(duty.getDutyId());
        for (Checklist checklist : checklists) {
            LocalDateTime createdAt = checklist.getCreatedAt();
            if (isToday && (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(endTime)) && now.toLocalDate().equals(createdAt.toLocalDate())) {
                rslt.add(checklist);
            }
            if (!isToday && (
                    (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(LocalTime.MAX) && createdAt.toLocalDate().isEqual(now.minusDays(1).toLocalDate())) ||
                            (createdAt.toLocalTime().isBefore(endTime) && createdAt.toLocalDate().isEqual(now.toLocalDate()))
            )) {
                rslt.add(checklist);
            }
        }
        return rslt;
    }
}
