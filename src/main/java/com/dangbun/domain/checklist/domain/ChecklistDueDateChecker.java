package com.dangbun.domain.checklist.domain;

import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

@Component
public class ChecklistDueDateChecker {

    public boolean isDueToday(Cleaning cleaning, List<CleaningDate> cleaningDates) {
        CleaningRepeatType repeatType = cleaning.getRepeatType();
        LocalDate today = LocalDate.now();

        if (repeatType == CleaningRepeatType.NONE) {
            return cleaningDates.stream()
                    .anyMatch(cd -> cd.getDate().isEqual(today));
        }

        if (repeatType == CleaningRepeatType.DAILY) {
            return true;
        }

        if (repeatType == CleaningRepeatType.WEEKLY) {
            DayOfWeek todayDow = today.getDayOfWeek();
            List<String> days = Arrays.stream(cleaning.getRepeatDays().split(","))
                    .toList();
            return days.contains(todayDow.name());
        }

        if (repeatType == CleaningRepeatType.MONTHLY_FIRST) {
            return today.getDayOfMonth() == 1;
        }

        if (repeatType == CleaningRepeatType.MONTHLY_LAST) {
            LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
            return today.getDayOfMonth() == lastDay.getDayOfMonth();
        }

        return false;
    }
}
