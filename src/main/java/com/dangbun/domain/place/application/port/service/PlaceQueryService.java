package com.dangbun.domain.place.application.port.service;

import com.dangbun.domain.checklist.refactor.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.refactor.adapter.out.persistence.SpringDataChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.duty.original.repository.DutyRepository;
import com.dangbun.domain.duty.refactor.application.port.out.DutyCommandPort;
import com.dangbun.domain.duty.refactor.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.refactor.domain.Duty;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyCommandPort;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.adapter.out.persistence.SpringDataMemberDutyRepository;
import com.dangbun.domain.notificationreceiver.adapter.out.persistence.SpringDataNotificationReceiverRepository;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.application.port.out.PlaceQueryPort;
import com.dangbun.domain.place.adapter.in.web.dto.response.DutyProgressDto;
import com.dangbun.domain.place.domain.Place;
import com.dangbun.domain.place.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.exception.custom.InviteCodeNotExistsException;
import com.dangbun.domain.place.application.port.in.query.DutyProgressResult;
import com.dangbun.domain.place.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.application.port.in.query.PlaceQuery;
import com.dangbun.domain.place.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.domain.Information;
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

import static com.dangbun.domain.duty.refactor.domain.Duty.*;
import static com.dangbun.domain.place.exception.status.PlaceExceptionResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceQueryService implements PlaceQuery {

    private final PlaceQueryPort placeQueryPort;
    private final DutyQueryPort dutyQueryPort;
    private final MemberDutyQueryPort memberDutyQueryPort;
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
    private final SpringDataChecklistRepository checklistRepository;
    private final CleaningRepository cleaningRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final SpringDataMemberDutyRepository memberDutyRepository;
    private final SpringDataNotificationReceiverRepository notificationReceiverRepository;
    private final DutyCommandPort dutyCommandPort;
    private final MemberDutyCommandPort memberDutyCommandPort;

    @Override
    public PlaceListResult getPlaceList(Long userId) {
        List<MemberJpaEntity> members = memberRepository.findWithPlaceByUserId(userId);

        List<PlaceListResult.PlaceDto> placeDtos = new ArrayList<>();

        for (MemberJpaEntity member : members) {
            if (!member.getStatus() || member.getRole().equals(MemberRole.WAITING)) {
                PlaceJpaEntity place = member.getPlace();
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
                PlaceJpaEntity place = member.getPlace();

                List<MemberCleaningJpaEntity> memberCleaningJpaEntities = memberCleaningRepository.findAllByMember(member);
                Integer totalCleaning = 0;
                Integer endCleaning = 0;
                LocalDate now = LocalDate.now();
                LocalDateTime start = now.atStartOfDay();
                LocalDateTime end = now.plusDays(1).atStartOfDay();

                if (member.getRole().equals(MemberRole.MANAGER)) {
                    List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findByPlace(place);
                    totalCleaning = cleaningJpaEntities.size();
                    for (CleaningJpaEntity cleaningJpaEntity : cleaningJpaEntities) {
                        if (checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaningJpaEntity)) {
                            endCleaning++;
                        }
                    }
                }
                if (member.getRole().equals(MemberRole.MEMBER)) {
                    totalCleaning = memberCleaningJpaEntities.size();
                    for (MemberCleaningJpaEntity memberCleaningJpaEntity : memberCleaningJpaEntities) {
                        CleaningJpaEntity cleaningJpaEntity = memberCleaningJpaEntity.getCleaningJpaEntity();

                        if (checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaningJpaEntity)) {
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
        Place place = placeQueryPort.findByInviteCode(invitedCode)
                .orElseThrow(() -> new InvalidInviteCodeException(INVALID_INVITE_CODE));

        User user = userRepository.findById(userId).orElseThrow();

        /*
         * TODO: Member 도메인 헥사고날 아키텍처 전환 시 수정
         * memberRepository.findByPlaceIdAndUserId() 메서드로 변경 필요
         */
        if (memberRepository.findWithPlaceByUserIdAndPlaceId(userId, place.getPlaceId().value()).isPresent()) {
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }

        /*
         * TODO: Member 도메인 헥사고날 아키텍처 전환 시 수정
         * MemberQueryPort를 통해 조회하도록 변경 필요
         */
        MemberJpaEntity member = memberRepository.findByPlace_PlaceId(place.getPlaceId().value()).stream().findFirst().orElseThrow();
        Set<String> information = member.getInformation().keySet();
        List<String> informationList = information.stream().toList();

        return new Information(place.getPlaceId().value(), informationList);
    }

    @Override
    public PlaceResult getPlace() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        MemberJpaEntity me = MemberContext.get();
        PlaceJpaEntity place = me.getPlace();
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

        List<MemberDuty> memberDuties = memberDutyQueryPort.findAllByPlaceId(placeId);
//        List<MemberDutyJpaEntity> memberDuties = memberDutyRepository.findAllWithMemberAndPlaceByPlaceId(placeId);

        List<MemberCleaningJpaEntity> memberCleaningJpaEntities = memberDuties.stream()
                .flatMap(md -> memberCleaningRepository.findAllByMember_MemberId(md.getMemberId()).stream())
                .distinct()
                .toList();

        if (me.getRole() == MemberRole.MANAGER) {
            List<Duty> duties = dutyQueryPort.findByPlaceId(placeId);
            List<DutyId> dutyIds = duties.stream().map(Duty::getDutyId).toList();
//            List<Duty> duties = dutyRepository.findByPlace_PlaceId(placeId);

            Map<DutyId, List<ChecklistJpaEntity>> checklistMap = dutyIds.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            d -> filterChecklist(d, place),
                            (existing, replacement) -> existing,
                            LinkedHashMap::new
                    ));

            PlaceResult.DutyDto dutyDto = createManagerDutyResult(checklistMap, memberCleaningJpaEntities);
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

        List<DutyId> duties = memberDuties.stream()
                .map(md -> new DutyId(md.getDutyId()))
                .distinct()
                .toList();

        Map<DutyId, List<ChecklistJpaEntity>> checklistMap = duties.stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        d -> filterChecklist(d, place),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        PlaceResult.DutyDto dutyDto = null;
        if (me.getRole() == MemberRole.MEMBER) {
            dutyDto = createMemberDutyResult(me, checklistMap, memberCleaningJpaEntities);
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
        PlaceJpaEntity place = MemberContext.get().getPlace();

        return new PlaceTimeResult(
                place.getStartTime(),
                place.getEndTime(),
                place.getIsToday()
        );
    }

    @Override
    public String getInviteCode() {
        // TODO: Member 도메인 헥사고날 아키텍처 전환 시 MemberContext 대신 전용 포트 사용
        PlaceJpaEntity place = MemberContext.get().getPlace();
        String code = place.getInviteCode();

        if (code == null || code.isBlank()) {
            throw new InviteCodeNotExistsException(INVITE_CODE_NOT_EXISTS);
        }
        return code;
    }

    private List<ChecklistJpaEntity> filterChecklist(DutyId dutyId, PlaceJpaEntity place) {
        List<ChecklistJpaEntity> result = new ArrayList<>();

        Boolean isToday = place.getIsToday();
        LocalDateTime now = LocalDateTime.now();
        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();

        List<ChecklistJpaEntity> checklistJpaEntities = checklistRepository.findWithCleaningByDutyId(dutyId.value());
        for (ChecklistJpaEntity checklistJpaEntity : checklistJpaEntities) {
            LocalDateTime createdAt = checklistJpaEntity.getCreatedAt();
            if (isToday && (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(endTime)) && now.toLocalDate().equals(createdAt.toLocalDate())) {
                result.add(checklistJpaEntity);
            }
            if (!isToday && (
                    (createdAt.toLocalTime().isAfter(startTime) && createdAt.toLocalTime().isBefore(LocalTime.MAX) && createdAt.toLocalDate().isEqual(now.minusDays(1).toLocalDate())) ||
                            (createdAt.toLocalTime().isBefore(endTime) && createdAt.toLocalDate().isEqual(now.toLocalDate()))
            )) {
                result.add(checklistJpaEntity);
            }
        }
        return result;
    }

    private PlaceResult.DutyDto createManagerDutyResult(Map<DutyId, List<ChecklistJpaEntity>> checklistMap, List<MemberCleaningJpaEntity> memberCleaningJpaEntities) {
        List<PlaceResult.CheckListDto> allCheckLists = new ArrayList<>();
        String dutyName = null;
        Long dutyId = null;

        for (Map.Entry<DutyId, List<ChecklistJpaEntity>> entry : checklistMap.entrySet()) {
            dutyId = entry.getKey().value();
            dutyName = dutyQueryPort.getDutyNameById(dutyId);

            for (ChecklistJpaEntity checklistJpaEntity : entry.getValue()) {
                CleaningJpaEntity cleaningJpaEntity = checklistJpaEntity.getCleaningJpaEntity();

                List<PlaceResult.MemberDto> members = new ArrayList<>();
                for (MemberCleaningJpaEntity mc : memberCleaningJpaEntities) {
                    if (mc.getCleaningJpaEntity().equals(cleaningJpaEntity)) {
                        MemberJpaEntity m = mc.getMember();
                        members.add(new PlaceResult.MemberDto(m.getMemberId(), m.getName()));
                    }
                }

                LocalTime completeTime = checklistJpaEntity.getCompleteTime() != null
                        ? checklistJpaEntity.getCompleteTime().toLocalTime()
                        : null;

                allCheckLists.add(new PlaceResult.CheckListDto(
                        checklistJpaEntity.getChecklistId(),
                        members,
                        cleaningJpaEntity.getName(),
                        completeTime,
                        cleaningJpaEntity.getNeedPhoto()
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

    private PlaceResult.DutyDto createMemberDutyResult(MemberJpaEntity me, Map<DutyId, List<ChecklistJpaEntity>> checklistMap, List<MemberCleaningJpaEntity> memberCleaningJpaEntities) {
        List<PlaceResult.CheckListDto> allCheckLists = new ArrayList<>();
        String dutyName = null;
        Long dutyId = null;

        for (Map.Entry<DutyId, List<ChecklistJpaEntity>> entry : checklistMap.entrySet()) {
            dutyId = entry.getKey().value();
//            dutyId = duty.getDutyId();
            dutyName = dutyQueryPort.getDutyNameById(dutyId);

            for (ChecklistJpaEntity checklistJpaEntity : entry.getValue()) {
                CleaningJpaEntity cleaningJpaEntity = checklistJpaEntity.getCleaningJpaEntity();

                List<PlaceResult.MemberDto> members = new ArrayList<>();
                boolean containsMe = false;

                for (MemberCleaningJpaEntity mc : memberCleaningJpaEntities) {
                    if (mc.getCleaningJpaEntity().equals(cleaningJpaEntity)) {
                        MemberJpaEntity m = mc.getMember();
                        members.add(new PlaceResult.MemberDto(m.getMemberId(), m.getName()));
                        if (m.equals(me)) {
                            containsMe = true;
                        }
                    }
                }

                if (containsMe) {
                    LocalTime completeTime = checklistJpaEntity.getCompleteTime() != null
                            ? checklistJpaEntity.getCompleteTime().toLocalTime()
                            : null;

                    allCheckLists.add(new PlaceResult.CheckListDto(
                            checklistJpaEntity.getChecklistId(),
                            members,
                            cleaningJpaEntity.getName(),
                            completeTime,
                            cleaningJpaEntity.getNeedPhoto()
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
