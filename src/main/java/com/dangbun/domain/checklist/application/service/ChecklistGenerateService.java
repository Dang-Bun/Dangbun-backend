package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.application.port.in.command.GenerateDailyChecklistsUseCase;
import com.dangbun.domain.checklist.application.port.out.ChecklistCommandPort;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.checklist.domain.ChecklistDueDateChecker;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForChecklistQuery;
import com.dangbun.domain.cleaning.application.port.in.query.GetCleaningForChecklistQuery.CleaningInfo;
import com.dangbun.domain.cleaningdate.application.port.in.query.GetCleaningDateForChecklistQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class ChecklistGenerateService implements GenerateDailyChecklistsUseCase {

    private final GetCleaningForChecklistQuery getCleaningForChecklistQuery;
    private final GetCleaningDateForChecklistQuery getCleaningDateForChecklistQuery;
    private final ChecklistQueryPort checklistQueryPort;
    private final ChecklistCommandPort checklistCommandPort;
    private final ChecklistDueDateChecker dueDateChecker;

    @Override
    public void generateDailyChecklists(LocalDateTime now) {
        List<CleaningInfo> cleanings = getCleaningForChecklistQuery.findAll();

        for (CleaningInfo cleaning : cleanings) {
            List<LocalDate> cleaningDates = getCleaningDateForChecklistQuery.findDatesByCleaningId(
                    cleaning.cleaningId()
            );

            if (!dueDateChecker.isDueToday(cleaning, cleaningDates)) {
                continue;
            }

            boolean exists = checklistQueryPort.existsByCleaningIdAndCreatedAt(
                    cleaning.cleaningId(),
                    now
            );

            if (exists) {
                continue;
            }

            checklistCommandPort.createChecklist(cleaning.cleaningId());
        }
    }
}
