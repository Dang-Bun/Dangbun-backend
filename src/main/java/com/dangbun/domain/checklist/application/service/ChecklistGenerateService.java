package com.dangbun.domain.checklist.application.service;

import com.dangbun.common.hexagonal.UseCase;
import com.dangbun.domain.checklist.application.port.in.command.GenerateDailyChecklistsUseCase;
import com.dangbun.domain.checklist.application.port.out.ChecklistCommandPort;
import com.dangbun.domain.checklist.application.port.out.ChecklistQueryPort;
import com.dangbun.domain.checklist.domain.ChecklistDueDateChecker;
import com.dangbun.domain.cleaning.application.port.out.CleaningQueryPort;
import com.dangbun.domain.cleaning.domain.Cleaning;
import com.dangbun.domain.cleaningdate.application.port.out.CleaningDateQueryPort;
import com.dangbun.domain.cleaningdate.domain.CleaningDate;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@UseCase
@RequiredArgsConstructor
@Transactional
public class ChecklistGenerateService implements GenerateDailyChecklistsUseCase {

    private final CleaningQueryPort cleaningQueryPort;
    private final CleaningDateQueryPort cleaningDateQueryPort;
    private final ChecklistQueryPort checklistQueryPort;
    private final ChecklistCommandPort checklistCommandPort;
    private final ChecklistDueDateChecker dueDateChecker;

    @Override
    public void generateDailyChecklists(LocalDateTime now) {
        List<Cleaning> cleanings = cleaningQueryPort.findAll();

        for (Cleaning cleaning : cleanings) {
            List<CleaningDate> cleaningDates = cleaningDateQueryPort.findByCleaningId(
                    cleaning.getCleaningId().value()
            );

            if (!dueDateChecker.isDueToday(cleaning, cleaningDates)) {
                continue;
            }

            boolean exists = checklistQueryPort.existsByCleaningIdAndCreatedAt(
                    cleaning.getCleaningId().value(),
                    now
            );

            if (exists) {
                continue;
            }

            checklistCommandPort.createChecklist(cleaning.getCleaningId().value());
        }
    }
}
