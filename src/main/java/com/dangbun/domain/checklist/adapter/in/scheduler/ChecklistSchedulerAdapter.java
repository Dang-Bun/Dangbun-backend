package com.dangbun.domain.checklist.adapter.in.scheduler;

import com.dangbun.domain.checklist.application.port.in.command.GenerateDailyChecklistsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ChecklistSchedulerAdapter {

    private final GenerateDailyChecklistsUseCase generateDailyChecklistsUseCase;


    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledChecklistGeneration() {
        generateDailyChecklistsUseCase.generateDailyChecklists(LocalDateTime.now());
    }
}
