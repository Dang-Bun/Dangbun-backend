package com.dangbun.domain.calender.service;

import com.dangbun.domain.calender.dto.GetChecklistsResponse;
import com.dangbun.domain.calender.dto.GetImageUrlResponse;
import com.dangbun.domain.calender.dto.GetProgressBarsResponse;
import com.dangbun.domain.calender.dto.PatchUpdateChecklistToCompleteResponse;
import com.dangbun.domain.calender.exception.custom.InvalidDateException;
import com.dangbun.domain.calender.exception.custom.InvalidRoleException;
import com.dangbun.domain.calender.exception.custom.NoPhotoException;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaningImage.service.CleaningImageService;
import com.dangbun.domain.member.MemberContext;
import com.dangbun.domain.member.entity.Member;
import com.dangbun.domain.member.entity.MemberRole;
import com.dangbun.domain.member.repository.MemberRepository;
import com.dangbun.domain.membercleaning.entity.MemberCleaning;
import com.dangbun.domain.membercleaning.repository.MemberCleaningRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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

    @Transactional(readOnly = true)
    public GetChecklistsResponse getChecklists(Long placeId, LocalDate date) {

        Member me = MemberContext.get();

        if(date.isAfter(LocalDate.now())){
            throw new InvalidDateException(FUTURE_DATE_NOT_ALLOWED);
        }
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<Checklist> checklists = checklistRepository.findAllByCreatedDateAndPlaceId(start, end, placeId);

        filterMyChecklists(me, checklists);

        List<ChecklistDto> checklistDtos = new ArrayList<>();

        for(Checklist checklist : checklists){
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
    public GetProgressBarsResponse getProgressBars(Long placeId, int year, int month) {
        Member me = MemberContext.get();
        YearMonth current = YearMonth.of(year, month);
        LocalDateTime start = current.minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime end = current.plusMonths(1).atDay(1).atStartOfDay();

        List<Checklist> checklists = checklistRepository.findByPlaceAndMonth(placeId, start, end);

        if(me.getRole().equals(MemberRole.MEMBER)){
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
        if(me.getRole().equals(MemberRole.MEMBER)){
            List<MemberCleaning> memberCleanings = memberCleaningRepository.findAllByMember(me);

            for(MemberCleaning memberCleaning : memberCleanings){
                checklists.removeIf(checklist -> !memberCleaning.getCleaning().equals(checklist.getCleaning()));
            }
        }
    }

    public PatchUpdateChecklistToCompleteResponse finishChecklist(Long placeId, Long checklistId) {
        Member me = MemberContext.get();
        if(me.getRole().equals(MemberRole.MEMBER)){
            throw new InvalidRoleException(INVALID_ROLE);
        }

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow();

        checklist.completeChecklist(me);

        return PatchUpdateChecklistToCompleteResponse.of(me.getName(), LocalTime.now());
    }

    public GetImageUrlResponse getPhotoUrl(Long placeId, Long checklistId) {
        Checklist checklist = checklistRepository.findWithCleaningById(checklistId).orElseThrow();

        if(!checklist.getCleaning().getNeedPhoto()){
            throw new NoPhotoException(NO_PHOTO);
        }

        String imageUrl = cleaningImageService.getImageUrl(checklistId);
        return GetImageUrlResponse.of(imageUrl);
    }
}
