package com.dangbun.domain.checklist.refactor.adapter.out.persistence;

import com.dangbun.common.hexagonal.PersistenceAdapter;
import com.dangbun.domain.checklist.refactor.application.port.out.ChecklistCommandPort;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
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

@PersistenceAdapter
@RequiredArgsConstructor
class ChecklistPersistenceAdapter implements ChecklistCommandPort {

    private final SpringDataChecklistRepository checklistRepository;
    private final SpringDataPlaceRepository placeRepository;
    private final CleaningRepository cleaningRepository;

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
}
