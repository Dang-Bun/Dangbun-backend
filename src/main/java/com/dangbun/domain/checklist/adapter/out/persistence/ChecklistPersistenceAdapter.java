package com.dangbun.domain.checklist.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.checklist.application.port.out.ChecklistCommandPort;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.checklist.domain.Checklist;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.cleaning.application.port.in.query.CleaningQuery;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import com.dangbun.domain.place.adapter.out.persistence.PlaceJpaEntity;
import com.dangbun.domain.place.adapter.out.persistence.SpringDataPlaceRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@PersistenceAdapter
@RequiredArgsConstructor
class ChecklistPersistenceAdapter implements ChecklistCommandPort, ChecklistQueryPort {

    private final SpringDataChecklistRepository checklistRepository;
    private final SpringDataPlaceRepository placeRepository;
    private final CleaningRepository cleaningRepository;

    private final ChecklistMapper checklistMapper;
    private final CleaningQuery cleaningQuery;

    @Override
    public void createChecklistByDateAndTime(Long cleaningId, List<CleaningDate> cleaningDates, Long placeId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        PlaceJpaEntity place = placeRepository.getReferenceById(placeId);
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.getReferenceById(cleaningId);

        if (checkDateState(nowTime, place)) {
            CleaningRepeatType repeatType = cleaningJpaEntity.getRepeatType();
            if (repeatType.equals(CleaningRepeatType.DAILY)) {
                createChecklist(cleaningJpaEntity);
            }

            if (repeatType.equals(CleaningRepeatType.WEEKLY)) {
                String repeatDays = cleaningJpaEntity.getRepeatDays();
                String[] days = repeatDays.split(",");
                for (String day : days) {
                    if (now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN).equals(day)) {
                        createChecklist(cleaningJpaEntity);
                    }
                }
            }

            if (repeatType.equals(CleaningRepeatType.MONTHLY_FIRST)) {
                if (nowDate.getDayOfMonth() == 1) {
                    createChecklist(cleaningJpaEntity);
                }
            }

            if (repeatType.equals(CleaningRepeatType.MONTHLY_LAST)) {
                if (nowDate.getDayOfMonth() == YearMonth.from(now).lengthOfMonth()) {
                    createChecklist(cleaningJpaEntity);
                }
            }

            if (repeatType.equals(CleaningRepeatType.NONE)) {
                for (CleaningDate cleaningDate : cleaningDates) {
                    if (nowDate.equals(cleaningDate.getDate())) {
                        createChecklist(cleaningJpaEntity);
                    }
                }
            }
        }
    }

    @Override
    public Checklist completeChecklist(Long checklistId, Long memberId) {
        ChecklistJpaEntity checklist = checklistRepository.findById(checklistId).get();
        checklist.completeChecklist(memberId);
        ChecklistJpaEntity save = checklistRepository.save(checklist);

        return checklistMapper.mapToDomainEntity(save);
    }

    @Override
    public Checklist incompleteChecklist(Long checklistId) {
        ChecklistJpaEntity checklist = checklistRepository.findById(checklistId).get();
        checklist.incompleteChecklist();
        return checklistMapper.mapToDomainEntity(checklistRepository.save(checklist));
    }

    private static boolean checkDateState(LocalTime nowTime, PlaceJpaEntity place) {
        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();
        Boolean isToday = place.getIsToday();

        return nowTime.isAfter(startTime) && ((nowTime.isBefore(endTime) && isToday) || (nowTime.isAfter(endTime) && !isToday));
    }

    private void createChecklist(CleaningJpaEntity cleaningJpaEntity) {
        ChecklistJpaEntity checklistJpaEntity = ChecklistJpaEntity.builder()
                .cleaningJpaEntity(cleaningJpaEntity)
                .completeMemberId(null)
                .completeTime(null)
                .isComplete(false)
                .build();
        checklistRepository.save(checklistJpaEntity);
    }

    @Override
    public Optional<Checklist> findById(Long checklistId) {
        return checklistRepository.findById(checklistId)
                .map(checklistMapper::mapToDomainEntity);
    }

    @Override
    public List<Checklist> findByCleaningId(Long cleaningId) {
        return checklistRepository.findByCleaningJpaEntity_CleaningId(cleaningId).stream()
                .map(checklistMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Checklist> findAllByCreatedDateAndPlaceId(LocalDateTime start, LocalDateTime end, Long placeId) {
        return checklistRepository.findAllByCreatedDateAndPlaceId(start, end, placeId).stream()
                .map(checklistMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public List<Checklist> findByPlaceAndMonth(Long placeId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return checklistRepository.findByPlaceAndMonth(placeId, startDateTime, endDateTime).stream()
                .map(checklistMapper::mapToDomainEntity)
                .toList();
    }

    @Override
    public boolean existsCompletedChecklistByDateAndCleaning(LocalDateTime start, LocalDateTime end, Long cleaningId) {
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.getReferenceById(cleaningId);
        return checklistRepository.existsCompletedChecklistByDateAndCleaning(start, end, cleaningJpaEntity);
    }

    @Override
    public Optional<Checklist> findByChecklistAndMemberId(Long checklistId, Long memberId) {
        return checklistRepository.findByChecklistAndMemberId(checklistId, memberId)
                .map(checklistMapper::mapToDomainEntity);
    }

    @Override
    public Cleaning getCleaningJpaEntity(Long checklistId) {
        ChecklistJpaEntity checklistJpaEntity = checklistRepository.findById(checklistId).get();
        return cleaningQuery.getCleaning(checklistJpaEntity.getCleaningJpaEntity().getCleaningId());
    }

    @Override
    public boolean existsByCleaningIdAndCreatedAt(Long cleaningId, LocalDateTime createdAt) {
        return checklistRepository.existsByCleaningIdAndCreatedAtDate(cleaningId, createdAt);
    }

    @Override
    public void createChecklist(Long cleaningId) {
        CleaningJpaEntity cleaningJpaEntity = cleaningRepository.getReferenceById(cleaningId);
        createChecklist(cleaningJpaEntity);
    }
}
