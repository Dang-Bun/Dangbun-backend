package com.dangbun.domain.place.application.port.service;

import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery.ChecklistCalendarInfo;
import com.dangbun.domain.checklist.application.port.in.query.GetCompletedChecklistQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningsByPlaceQuery;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.duty.application.port.out.DutyQueryPort;
import com.dangbun.domain.duty.domain.Duty;
import com.dangbun.domain.member.application.port.in.query.GetMembersByUserIdQuery;
import com.dangbun.domain.member.domain.Member;
import com.dangbun.domain.member.domain.MemberRole;
import com.dangbun.domain.membercleaning.application.port.in.query.GetCleaningInfoByMemberQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMembersByCleaningQuery;
import com.dangbun.domain.memberduty.application.port.out.MemberDutyQueryPort;
import com.dangbun.domain.memberduty.domain.MemberDuty;
import com.dangbun.domain.notificationreceiver.application.port.in.query.GetUnreadNotificationCountQuery;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.application.port.in.query.DutyProgressResult;
import com.dangbun.domain.place.application.port.in.query.GetPlaceEndTimeQuery;
import com.dangbun.domain.place.application.port.in.query.PlaceListResult;
import com.dangbun.domain.place.application.port.in.query.PlaceQuery;
import com.dangbun.domain.place.application.port.in.query.PlaceResult;
import com.dangbun.domain.place.application.port.in.query.PlaceTimeResult;
import com.dangbun.domain.place.application.port.out.PlaceQueryPort;
import com.dangbun.domain.place.domain.Information;
import com.dangbun.domain.place.domain.Place;
import com.dangbun.domain.place.exception.custom.AlreadyInvitedException;
import com.dangbun.domain.place.exception.custom.InvalidInviteCodeException;
import com.dangbun.domain.place.exception.custom.InviteCodeNotExistsException;
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

import static com.dangbun.domain.place.exception.status.PlaceExceptionResponse.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceQueryService implements PlaceQuery, GetPlaceEndTimeQuery {

    private final PlaceQueryPort placeQueryPort;
    private final DutyQueryPort dutyQueryPort;
    private final MemberDutyQueryPort memberDutyQueryPort;

    // 인커밍 포트 사용
    private final GetMembersByUserIdQuery getMembersByUserIdQuery;
    private final GetCleaningInfoByMemberQuery getCleaningInfoByMemberQuery;
    private final GetCompletedChecklistQuery getCompletedChecklistQuery;
    private final GetUnreadNotificationCountQuery getUnreadNotificationCountQuery;
    private final GetCleaningsByPlaceQuery getCleaningsByPlaceQuery;
    private final GetChecklistForCalendarQuery getChecklistForCalendarQuery;
    private final GetMembersByCleaningQuery getMembersByCleaningQuery;

    @Override
    public PlaceListResult getPlaceList(Long userId) {
        List<Member> members = getMembersByUserIdQuery.getMembersByUserId(userId);

        List<PlaceListResult.PlaceDto> placeDtos = new ArrayList<>();

        for (Member member : members) {
            Place place = placeQueryPort.findById(member.getPlaceId()).orElse(null);
            if (place == null) continue;

            if (!member.getStatus() || member.getRole() == MemberRole.WAITING) {
                placeDtos.add(new PlaceListResult.PlaceDto(
                        place.getPlaceId().value(),
                        place.getName(),
                        place.getCategory(),
                        place.getCategoryName(),
                        null,
                        null,
                        null,
                        null
                ));
            } else {
                Integer totalCleaning = 0;
                Integer endCleaning = 0;
                LocalDate now = LocalDate.now();
                LocalDateTime start = now.atStartOfDay();
                LocalDateTime end = now.plusDays(1).atStartOfDay();

                if (member.getRole() == MemberRole.MANAGER) {
                    List<Cleaning> cleanings = getCleaningsByPlaceQuery.getCleaningsByPlaceId(place.getPlaceId().value());
                    totalCleaning = cleanings.size();
                    for (Cleaning cleaning : cleanings) {
                        if (getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(start, end, cleaning.getCleaningId().value())) {
                            endCleaning++;
                        }
                    }
                }
                if (member.getRole() == MemberRole.MEMBER) {
                    List<Long> cleaningIds = getCleaningInfoByMemberQuery.getCleaningIdsByMemberId(member.getMemberId());
                    totalCleaning = cleaningIds.size();
                    for (Long cleaningId : cleaningIds) {
                        if (getCompletedChecklistQuery.existsCompletedChecklistByDateAndCleaningId(start, end, cleaningId)) {
                            endCleaning++;
                        }
                    }
                }

                Integer notifyNumber = getUnreadNotificationCountQuery.getUnreadCountByMemberId(member.getMemberId());

                placeDtos.add(new PlaceListResult.PlaceDto(
                        place.getPlaceId().value(),
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

        if (getMembersByUserIdQuery.getMemberByUserIdAndPlaceId(userId, place.getPlaceId().value()).isPresent()) {
            throw new AlreadyInvitedException(ALREADY_INVITED);
        }

        Member member = getMembersByUserIdQuery.getFirstMemberByPlaceId(place.getPlaceId().value())
                .orElseThrow();
        Set<String> information = member.getInformation().keySet();
        List<String> informationList = information.stream().toList();

        return new Information(place.getPlaceId().value(), informationList);
    }

    @Override
    public PlaceResult getPlace() {
        /*
         * TODO: MemberContext 헥사고날 아키텍처 전환 시 수정
         * MemberContext.get()이 MemberJpaEntity 대신 도메인 모델을 반환하도록 변경 필요
         */
        Long memberId = MemberContext.get().getMemberId();
        Long placeId = MemberContext.get().getPlace().getPlaceId();
        MemberRole role = MemberRole.valueOf(MemberContext.get().getRole().name());
        Boolean status = MemberContext.get().getStatus();

        Place place = placeQueryPort.findById(placeId).orElseThrow();

        if (!status || role == MemberRole.WAITING) {
            return new PlaceResult(
                    memberId,
                    placeId,
                    place.getName(),
                    place.getCategory(),
                    place.getCategoryName(),
                    null,
                    null
            );
        }

        // 오늘 날짜의 체크리스트 조회
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        List<ChecklistCalendarInfo> todayChecklists = getChecklistForCalendarQuery
                .findAllByCreatedDateAndPlaceId(start, end, placeId);

        PlaceResult.DutyDto dutyDto = null;

        if (role == MemberRole.MANAGER) {
            // MANAGER: 첫 번째 duty의 체크리스트 반환
            List<Duty> duties = dutyQueryPort.findByPlaceId(placeId);
            if (!duties.isEmpty()) {
                Duty firstDuty = duties.get(0);
                dutyDto = buildDutyDto(firstDuty, todayChecklists);
            }
        } else {
            // MEMBER: 해당 멤버가 속한 첫 번째 duty의 체크리스트 반환
            List<MemberDuty> memberDuties = memberDutyQueryPort.findAllByMemberId(memberId);
            if (!memberDuties.isEmpty()) {
                Long dutyId = memberDuties.get(0).getDutyId();
                Duty duty = dutyQueryPort.findById(dutyId).orElse(null);
                if (duty != null) {
                    dutyDto = buildDutyDto(duty, todayChecklists);
                }
            }
        }

        return new PlaceResult(
                memberId,
                placeId,
                place.getName(),
                place.getCategory(),
                place.getCategoryName(),
                place.getEndTime(),
                dutyDto
        );
    }

    private PlaceResult.DutyDto buildDutyDto(Duty duty, List<ChecklistCalendarInfo> allChecklists) {
        // 해당 duty의 체크리스트만 필터링
        List<ChecklistCalendarInfo> dutyChecklists = allChecklists.stream()
                .filter(cl -> duty.getName().equals(cl.dutyName()))
                .toList();

        List<PlaceResult.CheckListDto> checkListDtos = dutyChecklists.stream()
                .map(cl -> {
                    List<Member> members = getMembersByCleaningQuery.getMembersByCleaningId(cl.cleaningId());
                    List<PlaceResult.MemberDto> memberDtos = members.stream()
                            .map(m -> new PlaceResult.MemberDto(m.getMemberId(), m.getName()))
                            .toList();
                    return new PlaceResult.CheckListDto(
                            cl.checklistId(),
                            memberDtos,
                            cl.cleaningName(),
                            cl.getCompleteLocalTime(),
                            cl.needPhoto()
                    );
                })
                .toList();

        int endCleaning = (int) dutyChecklists.stream()
                .filter(ChecklistCalendarInfo::isComplete)
                .count();

        return new PlaceResult.DutyDto(
                duty.getDutyId().value(),
                duty.getName(),
                dutyChecklists.size(),
                endCleaning,
                checkListDtos
        );
    }

    @Override
    public DutyProgressResult getDutiesProgress() {
        /*
         * TODO: MemberContext 헥사고날 아키텍처 전환 시 수정
         */
        Long placeId = MemberContext.get().getPlace().getPlaceId();

        // TODO: DutyQueryPort에 getDutyProgressByPlaceToday 메서드 추가 필요
        List<DutyProgressResult.DutyProgressDto> resultDtos = new ArrayList<>();

        return new DutyProgressResult(resultDtos);
    }

    @Override
    public PlaceTimeResult getTimeAndIsToday() {
        /*
         * TODO: MemberContext 헥사고날 아키텍처 전환 시 수정
         */
        PlaceJpaEntity placeJpaEntity = MemberContext.get().getPlace();
        Long placeId = placeJpaEntity.getPlaceId();
        Place place = placeQueryPort.findById(placeId).orElseThrow();

        return new PlaceTimeResult(
                place.getStartTime(),
                place.getEndTime(),
                place.getIsToday()
        );
    }

    @Override
    public String getInviteCode() {
        /*
         * TODO: MemberContext 헥사고날 아키텍처 전환 시 수정
         */
        Long placeId = MemberContext.get().getPlace().getPlaceId();
        Place place = placeQueryPort.findById(placeId).orElseThrow();
        String code = place.getInviteCode();

        if (code == null || code.isBlank()) {
            throw new InviteCodeNotExistsException(INVITE_CODE_NOT_EXISTS);
        }
        return code;
    }

    @Override
    public LocalTime getEndTimeByPlaceId(Long placeId) {
        Place place = placeQueryPort.findById(placeId).orElseThrow();
        return place.getEndTime();
    }
}
