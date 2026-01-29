package com.dangbun.domain.place.refactor.application.port.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.duty.repository.DutyRepository;
import com.dangbun.domain.member.entity.MemberJpaEntity;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.domain.memberduty.entity.MemberDuty;
import com.dangbun.domain.memberduty.repository.MemberDutyRepository;
import com.dangbun.domain.notificationreceiver.repository.NotificationReceiverRepository;
/*
 * TODO: Place 도메인 헥사고날 아키텍처 전환 완료 후 수정 필요
 * - PlaceRepository 제거 후 PlaceQueryPort 사용
 * - checkInviteCode 메서드에서 placeRepository.findByInviteCode() 대신
 *   PlaceQueryPort.findByInviteCode() 사용
 * - MemberRepository.findByPlaceAndUser()가 Place 엔티티 대신
 *   placeId를 받도록 변경되면 Place 엔티티 의존성 완전 제거 가능
 */
import com.dangbun.domain.place.original.repository.PlaceRepository;
import com.dangbun.domain.place.refactor.adapter.in.web.dto.response.DutyProgressDto;
import com.dangbun.domain.place.refactor.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.refactor.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.refactor.exception.custom.InviteCodeNotExistsException;
import com.dangbun.domain.place.refactor.application.port.in.query.DutyProgressResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceQuery;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.refactor.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.refactor.domain.Information;
import com.dangbun.domain.user.entity.User;
import com.dangbun.domain.user.repository.UserRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dangbun.domain.place.refactor.exception.status.PlaceExceptionResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceQueryService implements PlaceQuery {

    private final PlaceRepository placeRepository;

    /*
     * TODO: 다른 도메인 헥사고날 아키텍처 전환 시 수정
     * 각 도메인의 Query Port를 통해 조회하도록 변경 필요
     * - MemberRepository -> MemberQueryPort
     * - UserRepository -> UserQueryPort
     * - DutyRepository -> DutyQueryPort
     * - ChecklistRepository -> ChecklistQueryPort
     * - CleaningRepository -> CleaningQueryPort
     * - MemberCleaningRepository -> MemberCleaningQueryPort
     * - MemberDutyRepository -> MemberDutyQueryPort
     * - NotificationReceiverRepository -> NotificationReceiverQueryPort
     */
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final DutyRepository dutyRepository;
    private final ChecklistRepository checklistRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final MemberDutyRepository memberDutyRepository;
    private final NotificationReceiverRepository notificationReceiverRepository;

    @Override
    public PlaceListResult getPlaceList(Long userId) {
        List<MemberJpaEntity> members = memberRepository.findWithPlaceByUserId(userId);

        List<PlaceListResult.PlaceDto> placeDtos = new ArrayList<>();

        for (MemberJpaEntity member : members) {
            if (!member.getStatus() || member.getRole().equals(MemberRole.WAITING)) {
                com.dangbun.domain.place.original.entity.Place place = member.getPlace();
                placeDtos.add(new PlaceListResult.PlaceDto(
                        place.getPlaceId(),
                        place.getName(),
                        place.getCategory(),
                        place.getCategoryName(),
                        null,
                        null,
                        null,
                        null
                ));
            } else {
                com.dangbun.domain.place.original.entity.Place place = member.getPlace();

                List<MemberCleaning> memberCleanings = memberCleaningRepository.findAllByMember(member);
                Integer totalCleaning = 0;
                Integer endCleaning = 0;
                LocalDate now = LocalDate.now();
                LocalDateTime start = now.atStartOfDay();
                LocalDateTime end = now.plusDays(1).atStartOfDay();

                if (member.getRole().equals(MemberRole.MANAGER)) {
                    List<Cleaning> cleanings = cleaningRepository.findByPlace(place);
                    totalCleaning = cleanings.size();
                    for (Cleaning cleaning : cleanings) {
                        if (checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaning)) {
                            endCleaning++;
                        }
                    }
                }
                if (member.getRole().equals(MemberRole.MEMBER)) {
                    totalCleaning = memberCleanings.size();
                    for (MemberCleaning memberCleaning : memberCleanings) {
                        Cleaning cleaning = memberCleaning.getCleaning();

                        if (checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaning)) {
                            endCleaning++;
                        }
                    }
                }

                Integer notifyNumber = notificationReceiverRepository.countUnreadByMemberId(member.getMemberId());

                placeDtos.add(new PlaceListResult.PlaceDto(
                        place.getPlaceId(),
                        place.getName(),
                        place.getCategory(),
                        place.getCategoryName(),
                        totalCleaning,
                        endCleaning,
                        member.getRole().getDisplayName(),
                        notifyNumber
                ));
            }
        }

        return new PlaceListResult(placeDtos);
    }

    @Override
    public Information checkInviteCode(Long userId, String invitedCode) {
        com.dangbun.domain.place.original.entity.Place originalPlace = placeRepository.findByInviteCode(invitedCode);
        if (originalPlace == null) {
            throw new InvalidInviteCodeException(INVALID_INVITE_CODE);
        }

        User user = userRepository.findById(userId).orElseThrow();

        if (memberRepository.findByPlaceAndUser(originalPlace, user).isPresent()) {
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }

        MemberJpaEntity member = memberRepository.findFirstByPlace(originalPlace);
        Set<String> information = member.getInformation().keySet();
        List<String> informationList = information.stream().toList();

        return new Information(originalPlace.getPlaceId(), informationList);
    }

    @Override
    public PlaceResult getPlace() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        MemberJpaEntity me = MemberContext.get();
        com.dangbun.domain.place.original.entity.Place place = me.getPlace();
        Long placeId = place.getPlaceId();

        if (!me.getStatus() || me.getRole() == MemberRole.WAITING) {
            return new PlaceResult(
                    me.getMemberId(),
                    placeId,
                    place.getName(),
                    place.getCategory(),
                    place.getCategoryName(),
                    null,
                    null
            );
        }

        List<MemberDuty> memberDuties = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);

        List<MemberCleaning> memberCleanings = memberDuties.stream()
                .flatMap(md -> memberCleaningRepository.findAllByMember(md.getMember()).stream())
                .distinct()
                .toList();

        if (me.getRole() == MemberRole.MANAGER) {
            List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);

            Map<Duty, List<Checklist>> checklistMap = duties.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            d -> filterChecklist(d, place),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            PlaceResult.DutyDto dutyDto = createManagerDutyResult(checklistMap, memberCleanings);
            return new PlaceResult(
                    me.getMemberId(),
                    placeId,
                    place.getName(),
                    place.getCategory(),
                    place.getCategoryName(),
                    place.getEndTime(),
                    dutyDto
            );
        }

        List<Duty> duties = memberDuties.stream().map(MemberDuty::getDuty).distinct().toList();

        Map<Duty, List<Checklist>> checklistMap = duties.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        d -> filterChecklist(d, place),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        PlaceResult.DutyDto dutyDto = null;
        if (me.getRole() == MemberRole.MEMBER) {
            dutyDto = createMemberDutyResult(me, checklistMap, memberCleanings);
        }

        return new PlaceResult(
                me.getMemberId(),
                placeId,
                place.getName(),
                place.getCategory(),
                place.getCategoryName(),
                place.getEndTime(),
                dutyDto
        );
    }

    @Override
    public DutyProgressResult getDutiesProgress() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        List<DutyProgressDto> dutyDtos = dutyRepository.findDutyProgressByPlaceToday(placeId);
        List<DutyProgressResult.DutyProgressDto> resultDtos = dutyDtos.stream()
                .map(dto -> new DutyProgressResult.DutyProgressDto(
                        dto.dutyId(),
                        dto.dutyName(),
                        dto.totalCleaning(),
                        dto.endCleaning()
                ))
                .toList();

        return new DutyProgressResult(resultDtos);
    }

    @Override
    public PlaceTimeResult getTimeAndIsToday() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        com.dangbun.domain.place.original.entity.Place place = MemberContext.get().getPlace();

        return new PlaceTimeResult(
                place.getStartTime(),
                place.getEndTime(),
                place.getIsToday()
        );
    }

    @Override
    public String getInviteCode() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        com.dangbun.domain.place.original.entity.Place place = MemberContext.get().getPlace();
        String code = place.getInviteCode();

        if (code == null || code.isBlank()) {
            throw new InviteCodeNotExistsException(INVITE_CODE_NOT_EXISTS);
        }
        return code;
    }

    private List<Checklist> filterChecklist(Duty duty, com.dangbun.domain.place.original.entity.Place place) {
        List<Checklist> result = new ArrayList<>();

        Boolean isToday = place.getIsToday();
        LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();

        List<Checklist> checklists = checklistRepository.findWithCleaningByDutyId(duty.getDutyId());
        for (Checklist checklist : checklists) {
            LocalDateTime createdAt = checklist.getCreatedAt();
            if (isToday && (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(endTime)) && now.toLocalDate().equals(createdAt.toLocalDate())) {
                result.add(checklist);
            }
            if (!isToday && (
                    (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(LocalTime.MAX) && createdAt.toLocalDate().isEqual(now.minusDays(1).toLocalDate())) ||
                            (createdAt.toLocalTime().isBefore(endTime) && createdAt.toLocalDate().isEqual(now.toLocalDate()))
            )) {
                result.add(checklist);
            }
        }
        return result;
    }

    private PlaceResult.DutyDto createManagerDutyResult(Map<Duty, List<Checklist>> checklistMap, List<MemberCleaning> memberCleanings) {
        List<PlaceResult.CheckListDto> allCheckLists = new ArrayList<>();
        String dutyName = null;
        Long dutyId = null;

        for (Map.Entry<Duty, List<Checklist>> entry : checklistMap.entrySet()) {
            Duty duty = entry.getKey();
            dutyId = duty.getDutyId();
            dutyName = duty.getName();

            for (Checklist checklist : entry.getValue()) {
                Cleaning cleaning = checklist.getCleaning();

                List<PlaceResult.MemberDto> members = new ArrayList<>();
                for (MemberCleaning mc : memberCleanings) {
                    if (mc.getCleaning().equals(cleaning)) {
                        MemberJpaEntity m = mc.getMember();
                        members.add(new PlaceResult.MemberDto(m.getMemberId(), m.getName()));
                    }
                }

                LocalTime completeTime = checklist.getCompleteTime() != null
                        ? checklist.getCompleteTime().toLocalTime()
                        : null;

                allCheckLists.add(new PlaceResult.CheckListDto(
                        checklist.getChecklistId(),
                        members,
                        cleaning.getName(),
                        completeTime,
                        cleaning.getNeedPhoto()
                ));
            }
        }

        if (allCheckLists.isEmpty()) {
            return null;
        }

        int endCleaning = (int) allCheckLists.stream()
                .filter(c -> c.completeTime() != null)
                .count();

        return new PlaceResult.DutyDto(
                dutyId,
                dutyName,
                allCheckLists.size(),
                endCleaning,
                allCheckLists
        );
    }

    private PlaceResult.DutyDto createMemberDutyResult(MemberJpaEntity me, Map<Duty, List<Checklist>> checklistMap, List<MemberCleaning> memberCleanings) {
        List<PlaceResult.CheckListDto> allCheckLists = new ArrayList<>();
        String dutyName = null;
        Long dutyId = null;

        for (Map.Entry<Duty, List<Checklist>> entry : checklistMap.entrySet()) {
            Duty duty = entry.getKey();
            dutyId = duty.getDutyId();
            dutyName = duty.getName();

            for (Checklist checklist : entry.getValue()) {
                Cleaning cleaning = checklist.getCleaning();

                List<PlaceResult.MemberDto> members = new ArrayList<>();
                boolean containsMe = false;

                for (MemberCleaning mc : memberCleanings) {
                    if (mc.getCleaning().equals(cleaning)) {
                        MemberJpaEntity m = mc.getMember();
                        members.add(new PlaceResult.MemberDto(m.getMemberId(), m.getName()));
                        if (m.equals(me)) {
                            containsMe = true;
                        }
                    }
                }

                if (containsMe) {
                    LocalTime completeTime = checklist.getCompleteTime() != null
                            ? checklist.getCompleteTime().toLocalTime()
                            : null;

                    allCheckLists.add(new PlaceResult.CheckListDto(
                            checklist.getChecklistId(),
                            members,
                            cleaning.getName(),
                            completeTime,
                            cleaning.getNeedPhoto()
                    ));
                }
            }
        }

        if (allCheckLists.isEmpty()) {
            return null;
        }

        int endCleaning = (int) allCheckLists.stream()
                .filter(c -> c.completeTime() != null)
                .count();

        return new PlaceResult.DutyDto(
                dutyId,
                dutyName,
                allCheckLists.size(),
                endCleaning,
                allCheckLists
        );
    }
}
