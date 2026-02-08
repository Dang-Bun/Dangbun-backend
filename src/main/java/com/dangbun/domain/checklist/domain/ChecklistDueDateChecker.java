package com.dangbun.domain.checklist.domain;

import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForChecklistQuery.CleaningInfo;
import com.dangbun.domain.cleaning.domain.CleaningRepeatType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

@Component
public class ChecklistDueDateChecker {

    public boolean isDueToday(CleaningInfo cleaning, List<LocalDate> cleaningDates) {
        CleaningRepeatType repeatType = cleaning.repeatType();
        LocalDate today = LocalDate.now();

        if (repeatType == CleaningRepeatType.NONE) {
            return cleaningDates.stream()
                    .anyMatch(date -> date.isEqual(today));
        }

        if (repeatType == CleaningRepeatType.DAILY) {
            return true;
        }

        if (repeatType == CleaningRepeatType.WEEKLY) {
            DayOfWeek todayDow = today.getDayOfWeek();
            List<String> days = Arrays.stream(cleaning.repeatDays().split(","))
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
