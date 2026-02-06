package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningdate.entity.CleaningDateJpaEntity;
import com.dangbun.domain.place.original.entity.Place;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CreateChecklistService {


    private final ChecklistRepository checklistRepository;

    @Transactional
    public void createChecklistByDateAndTime(CleaningJpaEntity cleaningJpaEntity, List<CleaningDateJpaEntity> cleaningDateJpaEntities, Place place) {


        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();



        if(checkDateState(nowTime, place)) {
            CleaningRepeatType repeatType = cleaningJpaEntity.getRepeatType();
            if(repeatType.equals(CleaningRepeatType.DAILY)){
                createChecklist(cleaningJpaEntity);
            }

            if(repeatType.equals(CleaningRepeatType.WEEKLY)) {
                String repeatDays = cleaningJpaEntity.getRepeatDays();
                String[] days = repeatDays.split(",");
                for(String day:days){
                    if(now.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN).equals(day)){
                        createChecklist(cleaningJpaEntity);
                    }
                }
            }

            if(repeatType.equals(CleaningRepeatType.MONTHLY_FIRST)){
                if(nowDate.getDayOfMonth()==1){
                    createChecklist(cleaningJpaEntity);
                }
            }


            if(repeatType.equals(CleaningRepeatType.MONTHLY_LAST)){
                if(nowDate.getDayOfMonth() == YearMonth.from(now).lengthOfMonth()){
                    createChecklist(cleaningJpaEntity);
                }

            }

            if(repeatType.equals(CleaningRepeatType.NONE)){
                for(CleaningDateJpaEntity cleaningDateJpaEntity : cleaningDateJpaEntities){
                    if(nowDate.equals(cleaningDateJpaEntity.getDate())){
                        createChecklist(cleaningJpaEntity);
                    }
                }

            }


        }
    }

    private static boolean checkDateState(LocalTime nowTime, Place place) {
        LocalTime startTime = place.getStartTime();
        LocalTime endTime = place.getEndTime();
        Boolean isToday = place.getIsToday();



        return nowTime.isAfter(startTime) && ((nowTime.isBefore(endTime) && isToday) || (nowTime.isAfter(endTime)&& !isToday));
    }

    private void createChecklist(CleaningJpaEntity cleaningJpaEntity) {
        Checklist checklist = Checklist.builder()
                .cleaningJpaEntity(cleaningJpaEntity)
                .completeMemberId(null)
                .completeTime(null)
                .isComplete(false)
                .build();
        checklistRepository.save(checklist);
    }
}
