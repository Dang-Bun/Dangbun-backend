package com.dangbun.domain.calendar.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetChecklistsResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetChecklistsResponse.ChecklistDto;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetCleaningInfoResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetImageUrlResponse;
import com.dangbun.domain.calendar.adapter.in.web.dto.response.GetProgressBarsResponse;
import com.dangbun.domain.calendar.application.port.in.query.CalendarQuery;
import com.dangbun.domain.calendar.exception.custom.InvalidDateException;
import com.dangbun.domain.calendar.exception.custom.NoPhotoException;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery.ChecklistCalendarInfo;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery.ChecklistWithCleaningAndDutyInfo;
import com.dangbun.domain.checklist.application.port.in.query.GetChecklistForCalendarQuery.ChecklistWithCleaningInfo;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningImage.application.port.in.query.CleaningImageQuery;
import com.dangbun.domain.cleaningdate.application.port.in.query.GetCleaningDateForCalendarQuery;
import com.dangbun.domain.member.adapter.out.persistence.MemberJpaEntity;
import com.dangbun.domain.member.adapter.out.persistence.MemberRole;
import com.dangbun.domain.member.application.port.in.query.GetMemberForCalendarQuery;
import com.dangbun.domain.membercleaning.application.port.in.query.GetMemberCleaningForCalendarQuery;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.calendar.response.status.CalendarExceptionResponse.FUTURE_DATE_NOT_ALLOWED;
import static com.dangbun.domain.calendar.response.status.CalendarExceptionResponse.NO_PHOTO;

@UseCase
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CalendarQueryService implements CalendarQuery {

    private final CleaningImageQuery cleaningImageQuery;
    private final GetChecklistForCalendarQuery getChecklistForCalendarQuery;
    private final GetMemberForCalendarQuery getMemberForCalendarQuery;
    private final GetMemberCleaningForCalendarQuery getMemberCleaningForCalendarQuery;
    private final GetCleaningDateForCalendarQuery getCleaningDateForCalendarQuery;

    @Override
    public GetChecklistsResponse getChecklists(LocalDate date) {
        MemberJpaEntity me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException(FUTURE_DATE_NOT_ALLOWED);
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<ChecklistCalendarInfo> checklists = getChecklistForCalendarQuery.findAllByCreatedDateAndPlaceId(start, end, placeId);

        List<ChecklistCalendarInfo> filteredChecklists = filterMyChecklists(me, checklists);

        List<ChecklistDto> checklistDtos = new ArrayList<>();

        for (ChecklistCalendarInfo checklist : filteredChecklists) {
            Long checklistId = checklist.checklistId();
            String cleaningName = checklist.cleaningName();
            String dutyName = checklist.dutyName();
            Boolean isComplete = checklist.isComplete();

            String memberName = null;
            LocalTime localTime = null;
            if (checklist.completeMemberId() != null) {
                memberName = getMemberForCalendarQuery.findMemberNameById(checklist.completeMemberId()).orElse(null);
                localTime = checklist.getCompleteLocalTime();
            }
            Boolean needPhoto = checklist.needPhoto();

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

        List<ChecklistCalendarInfo> checklists = getChecklistForCalendarQuery.findByPlaceAndMonth(placeId, start, end);

        List<ChecklistCalendarInfo> filteredChecklists = checklists;
        if (me.getRole().equals(MemberRole.MEMBER)) {
            filteredChecklists = filterMyChecklists(me, checklists);
        }

        Map<LocalDate, List<ChecklistCalendarInfo>> dailyGrouped = filteredChecklists.stream()
                .collect(Collectors.groupingBy(
                        ch -> ch.createdAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.toList()
                ));

        List<GetProgressBarsResponse.DailyProgressDto> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<ChecklistCalendarInfo>> entry : dailyGrouped.entrySet()) {
            LocalDate entryDate = entry.getKey();
            List<ChecklistCalendarInfo> list = entry.getValue();

            int total = list.size();
            int completed = (int) list.stream().filter(ChecklistCalendarInfo::isComplete).count();

            result.add(GetProgressBarsResponse.DailyProgressDto.of(entryDate, total, completed));
        }

        return GetProgressBarsResponse.of(result);
    }

    @Override
    public GetImageUrlResponse getPhotoUrl(Long checklistId) {
        ChecklistWithCleaningInfo checklist = getChecklistForCalendarQuery.findWithCleaningById(checklistId)
                .orElseThrow();

        if (!checklist.needPhoto()) {
            throw new NoPhotoException(NO_PHOTO);
        }

        String imageUrl = cleaningImageQuery.getImageUrl(checklistId);
        return GetImageUrlResponse.of(imageUrl);
    }

    @Override
    public GetCleaningInfoResponse getCleaningInfo(Long checklistId) {
        ChecklistWithCleaningAndDutyInfo checklist = getChecklistForCalendarQuery.findWithCleaningAndDutyById(checklistId)
                .orElseThrow();

        Long cleaningId = checklist.cleaningId();
        String dutyName = checklist.dutyName();
        List<String> membersName = getMemberCleaningForCalendarQuery.findMemberNamesByCleaningId(cleaningId);
        Boolean needPhoto = checklist.needPhoto();
        CleaningRepeatType repeatType = checklist.repeatType() != null
                ? CleaningRepeatType.valueOf(checklist.repeatType())
                : null;
        List<DayOfWeek> repeatDays = parseRepeatDaysToDayOfWeek(checklist.repeatDays());
        List<LocalDate> dates = getCleaningDateForCalendarQuery.findDatesByCleaningId(cleaningId);

        return GetCleaningInfoResponse.of(cleaningId, dutyName, membersName, needPhoto, repeatType, repeatDays, dates);
    }

    private List<ChecklistCalendarInfo> filterMyChecklists(MemberJpaEntity me, List<ChecklistCalendarInfo> checklists) {
        if (me.getRole().equals(MemberRole.MEMBER)) {
            List<Long> myCleaningIds = getMemberCleaningForCalendarQuery.findCleaningIdsByMemberId(me.getMemberId());

            return checklists.stream()
                    .filter(checklist -> myCleaningIds.contains(checklist.cleaningId()))
                    .toList();
        }
        return checklists;
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
