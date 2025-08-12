package com.dangbun.domain.place.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
import com.dangbun.domain.place.dto.request.PostCheckInviteCodeRequest;
import com.dangbun.domain.place.dto.response.*;
import com.dangbun.domain.place.entity.Place;
import com.dangbun.domain.place.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.repository.PlaceRepository;
import com.dangbun.domain.user.entity.User;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.ALREADY_INVITED;
import static com.dangbun.domain.place.response.status.PlaceExceptionResponse.INVALID_INVITE_CODE;

@Service
@RequiredArgsConstructor
public class PlaceQueryService {

    private final MemberRepository memberRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final ChecklistRepository checklistRepository;
    private final NotificationReceiverRepository notificationReceiverRepository;
    private final PlaceRepository placeRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final DutyRepository dutyRepository;

    @Transactional(readOnly = true)
    public GetPlaceListResponse getPlaces(Long userId) {

        List<Member> members = memberRepository.findWithPlaceByUserId(userId);

        List<GetPlaceListResponse.PlaceDto> placeDtos = new ArrayList<>();
        for (Member member : members) {
            if (!member.getStatus()) {
                Place place = member.getPlace();
                placeDtos.add(GetPlaceListResponse.PlaceDto.of(place.getPlaceId(), place.getName(), place.getCategory(), place.getCategoryName(),null,null, null, null));
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
                    if(checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaning)){
                        endCleaning++;
                    }
                }

                Integer notifyNumber = notificationReceiverRepository.countUnreadByMemberId(member.getMemberId());


                placeDtos.add(GetPlaceListResponse.PlaceDto.of(place.getPlaceId(), place.getName(),place.getCategory(),place.getCategoryName(), totalCleaning, endCleaning, member.getRole().getDisplayName(), notifyNumber));
            }
        }

        return GetPlaceListResponse.of(placeDtos);
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

    @Transactional(readOnly = true)
    public GetPlaceResponse getPlace() {
        Member member = MemberContext.get();
        Place place = member.getPlace();
        Long placeId = place.getPlaceId();

        if (!member.getStatus()) {
            return new GetPlaceResponse(member.getMemberId(), placeId, place.getName(), place.getCategory(), place.getCategoryName(), null, null);
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);

        List<MemberCleaning> memberCleanings = new ArrayList<>();
        for (MemberDuty memberDuty : memberDuties) {
            memberCleanings.addAll(memberCleaningRepository.findAllByMember(memberDuty.getMember()));
        }

        Map<MemberDuty, List<Checklist>> cleaningMap = memberDuties.stream()
                .collect(Collectors.toMap(
                        md -> md,
                        md -> checklistRepository.findWithCleaningByDutyId(md.getDuty().getDutyId())
                ));


        return GetPlaceResponse.of(member, place, cleaningMap, memberCleanings);
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
}
