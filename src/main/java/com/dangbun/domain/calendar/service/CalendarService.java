package com.dangbun.domain.calendar.service;

import com.dangbun.domain.calendar.dto.*;
import com.dangbun.domain.calendar.exception.custom.InvalidDateException;

import com.dangbun.domain.calendar.exception.custom.NoPhotoException;
import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningImage.repository.CleaningImageRepository;
import com.dangbun.domain.cleaningImage.service.CleaningImageService;
import com.dangbun.domain.cleaningdate.adapter.out.persistence.CleaningDateJpaEntity;
import com.dangbun.domain.cleaningdate.adapter.out.persistence.CleaningDateRepository;
import com.dangbun.domain.duty.refactor.adapter.out.persistence.DutyJpaEntity;
import com.dangbun.domain.member.original.entity.MemberJpaEntity;
import com.dangbun.domain.member.original.entity.MemberRole;
import com.dangbun.domain.member.original.repository.MemberRepository;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningJpaEntity;
import com.dangbun.domain.membercleaning.adapter.out.persistence.MemberCleaningRepository;
import com.dangbun.global.context.MemberContext;
import com.dangbun.global.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.dangbun.domain.calendar.dto.GetChecklistsResponse.*;
import static com.dangbun.domain.calendar.response.status.CalendarExceptionResponse.*;

@RequiredArgsConstructor
@Service
@Transactional
public class CalendarService {


    private final ChecklistRepository checklistRepository;
    private final MemberCleaningRepository memberCleaningRepository;
    private final MemberRepository memberRepository;
    private final CleaningImageService cleaningImageService;
    private final CleaningImageRepository cleaningImageRepository;
    private final CleaningDateRepository cleaningDateRepository;
    private final S3Service s3Service;


    @Transactional(readOnly = true)
    public GetChecklistsResponse getChecklists(LocalDate date) {

        MemberJpaEntity me = MemberContext.get();
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
            String cleaningName = checklist.getCleaningJpaEntity().getName();
            String dutyName = checklist.getCleaningJpaEntity().getDuty().getName();
            Boolean isComplete = checklist.getIsComplete();

            String memberName = null;
            LocalTime localTime = null;
            if (checklist.getCompleteMemberId() != null) {
                memberName = memberRepository.findById(checklist.getCompleteMemberId()).map(MemberJpaEntity::getName).orElse(null);
                localTime = checklist.getCompleteTime().toLocalTime();
            }
            Boolean needPhoto = checklist.getCleaningJpaEntity().getNeedPhoto();

            checklistDtos.add(ChecklistDto.of(checklistId, cleaningName, dutyName, isComplete, memberName, localTime, needPhoto));
        }

        return GetChecklistsResponse.of(checklistDtos);
    }


    @Transactional(readOnly = true)
    public GetProgressBarsResponse getProgressBars(int year, int month) {
        MemberJpaEntity me = MemberContext.get();
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

    private void filterMyChecklists(MemberJpaEntity me, List<Checklist> checklists) {
        if (me.getRole().equals(MemberRole.MEMBER)) {
            List<CleaningJpaEntity> myCleaningJpaEntities = memberCleaningRepository.findAllByMember(me)
                    .stream()
                    .map(MemberCleaningJpaEntity::getCleaningJpaEntity)
                    .toList();

            checklists.removeIf(checklist -> !myCleaningJpaEntities.contains(checklist.getCleaningJpaEntity()));
        }
    }

    public PatchUpdateChecklistToCompleteResponse finishChecklist(Long checklistId) {
        MemberJpaEntity me = MemberContext.get();

        Checklist checklist = checklistRepository.findById(checklistId)
                .orElseThrow();

        checklist.completeChecklist(me);

        return PatchUpdateChecklistToCompleteResponse.of(me.getName(), LocalTime.now());
    }

    public GetImageUrlResponse getPhotoUrl(Long checklistId) {
        Checklist checklist = checklistRepository.findWithCleaningById(checklistId).orElseThrow();

        if (!checklist.getCleaningJpaEntity().getNeedPhoto()) {
            throw new NoPhotoException(NO_PHOTO);
        }

        String imageUrl = cleaningImageService.getImageUrl(checklistId);
        return GetImageUrlResponse.of(imageUrl);
    }

    public GetCleaningInfoResponse getCleaningInfo(Long checklistId) {
        Checklist checklist = checklistRepository.findWithCleaningAndDutyById(checklistId).orElseThrow();

        CleaningJpaEntity cleaningJpaEntity = checklist.getCleaningJpaEntity();
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
        MemberJpaEntity me = MemberContext.get();

        cleaningImageRepository.findByChecklist_ChecklistId(checklistId)
                .ifPresent(img -> s3Service.deleteFile(img.getS3Key()));

        checklistRepository.deleteById(checklistId);
    }
}
