package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningJpaEntity;
import com.dangbun.domain.cleaning.adapter.out.persistence.CleaningRepository;
import com.dangbun.domain.cleaningdate.entity.CleaningDateJpaEntity;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import static com.dangbun.domain.cleaning.domain.CleaningRepeatType.*;

@RequiredArgsConstructor
@Service
public class ChecklistGenerateService {

    private final CleaningDateRepository cleaningDateRepository;
    private final ChecklistRepository checklistRepository;
    private final CleaningRepository cleaningRepository;


    public boolean isDueToday(CleaningJpaEntity cleaningJpaEntity) {

        if (cleaningJpaEntity.getRepeatType().equals(NONE)) {
            List<CleaningDateJpaEntity> cleaningDateJpaEntities = cleaningDateRepository.findByCleaningJpaEntity(cleaningJpaEntity);

            for (CleaningDateJpaEntity cleaningDateJpaEntity : cleaningDateJpaEntities) {
                if (cleaningDateJpaEntity.getDate().isEqual(LocalDate.now())) {
                    return true;
                }
            }
        }

        if (cleaningJpaEntity.getRepeatType().equals(DAILY)) {
            return true;
        }


        LocalDate now = LocalDate.now();
        DayOfWeek dow = now.getDayOfWeek();

        if (cleaningJpaEntity.getRepeatType().equals(WEEKLY)) {
            List<String> days = Arrays.stream(cleaningJpaEntity.getRepeatDays().split(","))
                    .toList();

            if (days.contains(dow.name())) {
                return true;
            }
        }


        if (cleaningJpaEntity.getRepeatType().equals(MONTHLY_FIRST)) {
            if (now.getDayOfMonth() == 1) {
                return true;
            }
        }

        if (cleaningJpaEntity.getRepeatType().equals(MONTHLY_LAST)) {
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            if (now.getDayOfMonth() == lastDay.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    public void generateDailyChecklists(LocalDateTime now) {
        List<CleaningJpaEntity> cleaningJpaEntities = cleaningRepository.findAll();

        for (CleaningJpaEntity cleaningJpaEntity : cleaningJpaEntities) {
            if (!isDueToday(cleaningJpaEntity)) continue;

            Boolean exists = checklistRepository.existsByCleaningJpaEntityAndCreatedAt(cleaningJpaEntity, now);

            if (exists) continue;

            Checklist checklist = Checklist.builder()
                    .cleaningJpaEntity(cleaningJpaEntity)
                    .isComplete(false)
                    .completeMemberId(null)
                    .completeTime(null)
                    .build();

            checklistRepository.save(checklist);
        }
    }
}
