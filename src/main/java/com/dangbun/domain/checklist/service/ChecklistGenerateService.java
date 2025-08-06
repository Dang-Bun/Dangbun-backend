package com.dangbun.domain.checklist.service;

import com.dangbun.domain.checklist.entity.Checklist;
import com.dangbun.domain.checklist.repository.ChecklistRepository;
import com.dangbun.domain.cleaning.entity.Cleaning;
import com.dangbun.domain.cleaning.repository.CleaningRepository;
import com.dangbun.domain.cleaningdate.entity.CleaningDate;
import com.dangbun.domain.cleaningdate.repository.CleaningDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

import static com.dangbun.domain.cleaning.entity.CleaningRepeatType.*;

@RequiredArgsConstructor
@Service
public class ChecklistGenerateService {

    private final CleaningDateRepository cleaningDateRepository;
    private final ChecklistRepository checklistRepository;
    private final CleaningRepository cleaningRepository;


    public boolean isDueToday(Cleaning cleaning) {

        if (cleaning.getRepeatType().equals(NONE)) {
            List<CleaningDate> cleaningDates = cleaningDateRepository.findByCleaning(cleaning);

            for (CleaningDate cleaningDate : cleaningDates) {
                if (cleaningDate.getDate().isEqual(LocalDate.now())) {
                    return true;
                }
            }
        }

        if (cleaning.getRepeatType().equals(DAILY)) {
            return true;
        }


        LocalDate now = LocalDate.now();
        DayOfWeek dow = now.getDayOfWeek();

        if (cleaning.getRepeatType().equals(WEEKLY)) {
            List<String> days = Arrays.stream(cleaning.getRepeatDays().split(","))
                    .toList();

            if (days.contains(dow.name())) {
                return true;
            }
        }


        if (cleaning.getRepeatType().equals(MONTHLY_FIRST)) {
            if (now.getDayOfMonth() == 1) {
                return true;
            }
        }

        if (cleaning.getRepeatType().equals(MONTHLY_LAST)) {
            LocalDate lastDay = now.with(TemporalAdjusters.lastDayOfMonth());
            if (now.getDayOfMonth() == lastDay.getDayOfMonth()) {
                return true;
            }
        }
        return false;
    }

    public void generateDailyChecklists(LocalDateTime now) {
        List<Cleaning> cleanings = cleaningRepository.findAll();

        for (Cleaning cleaning : cleanings) {
            if (!isDueToday(cleaning)) continue;

            Boolean exists = checklistRepository.existsByCleaningAndCreatedAt(cleaning, now);

            if (exists) continue;

            Checklist checklist = Checklist.builder()
                    .cleaning(cleaning)
                    .isComplete(false)
                    .completeMemberId(null)
                    .completeTime(null)
                    .build();

            checklistRepository.save(checklist);
        }
    }
}
