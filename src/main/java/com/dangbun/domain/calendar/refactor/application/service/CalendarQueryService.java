package com.dangbun.domain.calendar.refactor.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetChecklistsResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetChecklistsResponse.ChecklistDto;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetCleaningInfoResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.calendar.refactor.adapter.in.web.dto.response.GetProgressBarsResponse;
import com.dangbun.domain.calendar.refactor.application.port.in.query.CalendarQuery;
import com.dangbun.domain.calendar.refactor.exception.custom.InvalidDateException;
import com.dangbun.domain.calendar.refactor.exception.custom.NoPhotoException;
import com.dangbun.domain.checklist.adapter.out.persistence.ChecklistJpaEntity;
import com.dangbun.domain.checklist.adapter.out.persistence.SpringDataChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.domain.cleaningdate.adapter.out.persistence.CleaningDateJpaEntity;
import com.dangbun.domain.cleaningdate.adapter.out.persistence.CleaningDateRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRepository;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.calendar.refactor.response.status.CalendarExceptionResponse.FUTURE_DATE_NOT_ALLOWED;
import static com.dangbun.domain.calendar.refactor.response.status.CalendarExceptionResponse.NO_PHOTO;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarQueryService implements CalendarQuery {

    private final CleaningImageQuery cleaningImageQuery;

    /*
     * TODO: Checklist/MemberCleaning/CleaningDate 도메인 헥사고날 아키텍처 전환 시 수정
     * ChecklistRepository -> ChecklistQueryPort
     * MemberCleaningRepository -> MemberCleaningQueryPort
     * CleaningDateRepository -> CleaningDateQueryPort
     * MemberRepository -> MemberQueryPort
     */
    private final SpringDataChecklistRepository checklistRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningDateRepository cleaningDateRepository;

    @Override
    public GetChecklistsResponse getChecklists(LocalDate date) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException(FUTURE_DATE_NOT_ALLOWED);
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<ChecklistJpaEntity> checklistJpaEntities = checklistRepository.findAllByCreatedDateAndPlaceId(start, end, placeId);

        filterMyChecklists(me, checklistJpaEntities);

        List<ChecklistDto> checklistDtos = new ArrayList<>();

        for (ChecklistJpaEntity checklistJpaEntity : checklistJpaEntities) {
            Long checklistId = checklistJpaEntity.getChecklistId();
            String cleaningName = checklistJpaEntity.getCleaningJpaEntity().getName();
            String dutyName = checklistJpaEntity.getCleaningJpaEntity().getDuty().getName();
            Boolean isComplete = checklistJpaEntity.getIsComplete();

            String memberName = null;
            LocalTime localTime = null;
            if (checklistJpaEntity.getCompleteMemberId() != null) {
                memberName = memberRepository.findById(checklistJpaEntity.getCompleteMemberId()).map(MemberJpaEntity::getName).orElse(null);
                localTime = checklistJpaEntity.getCompleteTime().toLocalTime();
            }
            Boolean needPhoto = checklistJpaEntity.getCleaningJpaEntity().getNeedPhoto();

            checklistDtos.add(ChecklistDto.of(checklistId, cleaningName, dutyName, isComplete, memberName, localTime, needPhoto));
        }

        return GetChecklistsResponse.of(checklistDtos);
    }

    @Override
    public GetProgressBarsResponse getProgressBars(int year, int month) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();
        YearMonth current = YearMonth.of(year, month);
        LocalDateTime start = current.minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime end = current.plusMonths(1).atDay(1).atStartOfDay();

        List<ChecklistJpaEntity> checklistJpaEntities = checklistRepository.findByPlaceAndMonth(placeId, start, end);

        if (me.getRole().equals(MemberRole.MEMBER)) {
            filterMyChecklists(me, checklistJpaEntities);
        }

        Map<LocalDate, List<ChecklistJpaEntity>> dailyGrouped = checklistJpaEntities.stream().collect(Collectors.groupingBy(ch -> ch.getCreatedAt().toLocalDate(),
                TreeMap::new, Collectors.toList()));

        List<GetProgressBarsResponse.DailyProgressDto> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<ChecklistJpaEntity>> entry : dailyGrouped.entrySet()) {
            LocalDate entryDate = entry.getKey();
            List<ChecklistJpaEntity> list = entry.getValue();

            int total = list.size();
            int completed = (int) list.stream().filter(ChecklistJpaEntity::getIsComplete).count();

            result.add(GetProgressBarsResponse.DailyProgressDto.of(entryDate, total, completed));
        }

        return GetProgressBarsResponse.of(result);
    }

    @Override
    public GetImageUrlResponse getPhotoUrl(Long checklistId) {
        ChecklistJpaEntity checklistJpaEntity = checklistRepository.findWithCleaningById(checklistId).orElseThrow();

        if (!checklistJpaEntity.getCleaningJpaEntity().getNeedPhoto()) {
            throw new NoPhotoException(NO_PHOTO);
        }

        String imageUrl = cleaningImageQuery.getImageUrl(checklistId);
        return GetImageUrlResponse.of(imageUrl);
    }

    @Override
    public GetCleaningInfoResponse getCleaningInfo(Long checklistId) {
        ChecklistJpaEntity checklistJpaEntity = checklistRepository.findWithCleaningAndDutyById(checklistId).orElseThrow();

        CleaningJpaEntity cleaningJpaEntity = checklistJpaEntity.getCleaningJpaEntity();
        DutyJpaEntity duty = cleaningJpaEntity.getDuty();
        List<MemberCleaningJpaEntity> memberCleaningJpaEntities = memberCleaningRepository.findAllByCleaningJpaEntity(cleaningJpaEntity);
        List<MemberJpaEntity> members = memberCleaningJpaEntities.stream().map(MemberCleaningJpaEntity::getMember).toList();

        Long cleaningId = cleaningJpaEntity.getCleaningId();
        String dutyName = duty.getName();
        List<String> membersName = members.stream().map(MemberJpaEntity::getName).toList();
        Boolean needPhoto = cleaningJpaEntity.getNeedPhoto();
        CleaningRepeatType repeatType = cleaningJpaEntity.getRepeatType();
        List<DayOfWeek> repeatDays = parseRepeatDaysToDayOfWeek(cleaningJpaEntity.getRepeatDays());

        List<CleaningDateJpaEntity> cleaningDateJpaEntities = cleaningDateRepository.findByCleaningJpaEntity(cleaningJpaEntity);

        List<LocalDate> dates = cleaningDateJpaEntities.stream().map(CleaningDateJpaEntity::getDate).toList();

        return GetCleaningInfoResponse.of(cleaningId, dutyName, membersName, needPhoto, repeatType, repeatDays, dates);
    }

    private void filterMyChecklists(MemberJpaEntity me, List<ChecklistJpaEntity> checklistJpaEntities) {
        if (me.getRole().equals(MemberRole.MEMBER)) {
            List<CleaningJpaEntity> myCleaningJpaEntities = memberCleaningRepository.findAllByMember(me)
                    .stream()
                    .map(MemberCleaningJpaEntity::getCleaningJpaEntity)
                    .toList();

            checklistJpaEntities.removeIf(checklist -> !myCleaningJpaEntities.contains(checklist.getCleaningJpaEntity()));
        }
    }

    private static List<DayOfWeek> parseRepeatDaysToDayOfWeek(String repeatDays) {
        if (repeatDays == null || repeatDays.isBlank()) {
            return List.of();
        }
        return Arrays.stream(repeatDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }
}
