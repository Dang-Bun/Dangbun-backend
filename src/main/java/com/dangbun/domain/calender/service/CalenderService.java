package com.dangbun.domain.calender.service;

import com.dangbun.domain.calender.dto.*;
import com.dangbun.domain.calender.exception.custom.InvalidDateException;

import com.dangbun.domain.calender.exception.custom.NoPhotoException;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.checklist.service.ChecklistService;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.entity.CleaningRepeatType;
import com.dangbun.domain.cleaningImage.service.CleaningImageService;
import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import com.dangbun.domain.duty.entity.Duty;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import com.dangbun.global.context.MemberContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.calender.dto.GetChecklistsResponse.*;
import static com.dangbun.domain.calender.response.status.CalenderExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class CalenderService {


    private final ChecklistRepository checklistRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningImageService cleaningImageService;
    private final CleaningDateRepository cleaningDateRepository;
    private final ChecklistService checklistService;

    @Transactional(readOnly = true)
    public GetChecklistsResponse getChecklists(LocalDate date) {

        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();

        if (date.isAfter(LocalDate.now())) {
            throw new InvalidDateException(FUTURE_DATE_NOT_ALLOWED);
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Checklist> checklists = checklistRepository.findAllByCreatedDateAndPlaceId(start, end, placeId);

        filterMyChecklists(me, checklists);

        List<ChecklistDto> checklistDtos = new ArrayList<>();

        for (Checklist checklist : checklists) {
            Long checklistId = checklist.getChecklistId();
            String dutyName = checklist.getCleaning().getDuty().getName();
            Boolean isComplete = checklist.getIsComplete();
            String memberName = memberRepository.findById(checklist.getCompleteMemberId()).get().getName();
            LocalTime localTime = checklist.getCompleteTime().toLocalTime();
            Boolean needPhoto = checklist.getCleaning().getNeedPhoto();

            checklistDtos.add(ChecklistDto.of(checklistId, dutyName, isComplete, memberName, localTime, needPhoto));
        }

        return GetChecklistsResponse.of(checklistDtos);
    }


    @Transactional(readOnly = true)
    public GetProgressBarsResponse getProgressBars(int year, int month) {
        Member me = MemberContext.get();
        Long placeId = me.getPlace().getPlaceId();
        YearMonth current = YearMonth.of(year, month);
        LocalDateTime start = current.minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime end = current.plusMonths(1).atDay(1).atStartOfDay();

        List<Checklist> checklists = checklistRepository.findByPlaceAndMonth(placeId, start, end);

        if (me.getRole().equals(MemberRole.MEMBER)) {
            filterMyChecklists(me, checklists);
        }

        Map<LocalDate, List<Checklist>> dailyGrouped = checklists.stream().collect(Collectors.groupingBy(ch -> ch.getCreatedAt().toLocalDate(),
                TreeMap::new, Collectors.toList()));

        List<GetProgressBarsResponse.DailyProgressDto> result = new ArrayList<>();

        for (Map.Entry<LocalDate, List<Checklist>> entry : dailyGrouped.entrySet()) {
            LocalDate date = entry.getKey();
            List<Checklist> list = entry.getValue();

            int total = list.size();
            int completed = (int) list.stream().filter(Checklist::getIsComplete).count();

            result.add(GetProgressBarsResponse.DailyProgressDto.of(date, total, completed));
        }

        return GetProgressBarsResponse.of(result);

    }

    private void filterMyChecklists(Member me, List<Checklist> checklists) {
        if (me.getRole().equals(MemberRole.MEMBER)) {
            List<MemberCleaning> memberCleanings = memberCleaningRepository.findAllByMember(me);

            for (MemberCleaning memberCleaning : memberCleanings) {
                checklists.removeIf(checklist -> !memberCleaning.getCleaning().equals(checklist.getCleaning()));
            }
        }
    }

    public PatchUpdateChecklistToCompleteResponse finishChecklist(Long checklistId) {
        Member me = MemberContext.get();

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow();

        checklist.completeChecklist(me);

        return PatchUpdateChecklistToCompleteResponse.of(me.getName(), LocalTime.now());
    }

    public GetImageUrlResponse getPhotoUrl(Long checklistId) {
        Checklist checklist = checklistRepository.findWithCleaningById(checklistId).orElseThrow();

        if (!checklist.getCleaning().getNeedPhoto()) {
            throw new NoPhotoException(NO_PHOTO);
        }

        String imageUrl = cleaningImageService.getImageUrl(checklistId);
        return GetImageUrlResponse.of(imageUrl);
    }

    public GetCleaningInfoResponse getCleaningInfo( Long checklistId) {
        Checklist checklist = checklistRepository.findWithCleaningAndDutyById(checklistId).orElseThrow();

        Cleaning cleaning = checklist.getCleaning();
        Duty duty = cleaning.getDuty();
        List<MemberCleaning> memberCleanings = memberCleaningRepository.findAllByCleaning(cleaning);
        List<Member> members = memberCleanings.stream().map(MemberCleaning::getMember).toList();

        Long cleaningId = cleaning.getCleaningId();
        String dutyName = duty.getName();
        List<String> membersName = members.stream().map(Member::getName).toList();
        Boolean needPhoto = cleaning.getNeedPhoto();
        CleaningRepeatType repeatType = cleaning.getRepeatType();
        List<DayOfWeek> repeatDays = parseRepeatDaysToDayOfWeek(cleaning.getRepeatDays());

        List<CleaningDate> cleaningDates = cleaningDateRepository.findByCleaning(cleaning);

        List<LocalDate> dates = cleaningDates.stream().map(CleaningDate::getDate).toList();

        return GetCleaningInfoResponse.of(cleaningId, dutyName, membersName, needPhoto, repeatType, repeatDays, dates);

    }


    public static List<DayOfWeek> parseRepeatDaysToDayOfWeek(String repeatDays) {
        if (repeatDays == null || repeatDays.isBlank()) {
            return List.of(); // 빈 리스트
        }
        return Arrays.stream(repeatDays.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toList());
    }

    public void deleteChecklist(Long checklistId) {
        Member me = MemberContext.get();

        checklistService.deleteChecklist(checklistId);
    }
}
